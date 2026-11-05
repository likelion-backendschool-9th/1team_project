// 로컬 백엔드의 DM 기능을 한 번에 확인한다.
// 테스트 회원 2명을 준비하고(없으면 만들고) 로그인 토큰을 만든 뒤,
// WebSocket으로 메시지를 주고받고 REST API(대화방 목록·대화 내역·읽음)를 확인한다.
//
// 실행: JWT_SECRET=<백엔드와 같은 값> npm run check
import crypto from "node:crypto";
import { Client } from "@stomp/stompjs";
import pg from "pg";
import WebSocket from "ws";

const API_BASE = process.env.API_BASE ?? "http://localhost:8080";
const WS_URL = process.env.WS_URL ?? API_BASE.replace(/^http/, "ws") + "/ws";
// 백엔드 FRONTEND_BASE_URL과 같아야 WebSocket 연결이 허용된다.
const ORIGIN = process.env.ORIGIN ?? "http://localhost:3000";
const DATABASE_URL = process.env.DATABASE_URL ?? "postgres://dipsource:dipsource@localhost:5432/dipsource";
const JWT_SECRET = process.env.JWT_SECRET;

if (!JWT_SECRET) {
  console.error("JWT_SECRET이 필요해요. 백엔드를 띄울 때 쓴 값과 같아야 합니다.");
  console.error("예: JWT_SECRET=... npm run check");
  process.exit(1);
}

let failed = 0;
function check(name, ok, detail = "") {
  console.log(`${ok ? "✅" : "❌"} ${name}${detail ? ` — ${detail}` : ""}`);
  if (!ok) failed++;
}

// ---------- 준비: 테스트 회원과 토큰 ----------

async function prepareUsers() {
  const db = new pg.Client({ connectionString: DATABASE_URL });
  await db.connect();
  const ids = [];
  for (const [email, nickname] of [
    ["dm-check-a@test.local", "dmcheck_a"],
    ["dm-check-b@test.local", "dmcheck_b"],
  ]) {
    const found = await db.query("select id from users where email = $1", [email]);
    if (found.rows.length > 0) {
      ids.push(Number(found.rows[0].id));
      continue;
    }
    const created = await db.query(
      `insert into users (email, nickname, role, status, chip_balance, blog_subscription_price, created_at, updated_at)
       values ($1, $2, 'USER', 'ACTIVE', 0, 0, now(), now()) returning id`,
      [email, nickname],
    );
    ids.push(Number(created.rows[0].id));
  }
  await db.end();
  return ids;
}

// 백엔드 JwtProvider와 같은 형식(HS256, sub = 회원 ID, type = ACCESS)의 Access Token
function accessToken(userId) {
  const encode = (value) => Buffer.from(JSON.stringify(value)).toString("base64url");
  const now = Math.floor(Date.now() / 1000);
  const body = `${encode({ alg: "HS256" })}.${encode({ sub: String(userId), iat: now, exp: now + 600, role: "USER", type: "ACCESS" })}`;
  const signature = crypto.createHmac("sha256", JWT_SECRET).update(body).digest("base64url");
  return `${body}.${signature}`;
}

// ---------- 도우미 ----------

async function get(path, token) {
  const response = await fetch(API_BASE + path, {
    headers: token ? { Cookie: `access_token=${token}` } : {},
  });
  return { status: response.status, body: await response.json().catch(() => null) };
}

function connect(token) {
  return new Promise((resolve, reject) => {
    const inbox = [];
    const errors = [];
    const client = new Client({
      webSocketFactory: () =>
        new WebSocket(WS_URL, ["v12.stomp"], {
          headers: { Origin: ORIGIN, ...(token ? { Cookie: `access_token=${token}` } : {}) },
        }),
      reconnectDelay: 0,
      onConnect: () => {
        client.subscribe("/user/queue/dm", (frame) => inbox.push(JSON.parse(frame.body)));
        client.subscribe("/user/queue/errors", (frame) => errors.push(JSON.parse(frame.body)));
        resolve({ client, inbox, errors });
      },
      onWebSocketClose: () => reject(new Error("연결이 거부됐어요")),
    });
    client.activate();
    setTimeout(() => reject(new Error("연결 시간 초과")), 5000);
  });
}

const wait = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

// ---------- 확인 ----------

console.log(`백엔드 ${API_BASE} · WebSocket ${WS_URL} · Origin ${ORIGIN}\n`);

let aId, bId;
try {
  [aId, bId] = await prepareUsers();
  console.log(`테스트 회원 A=${aId}, B=${bId}\n`);
} catch (error) {
  console.error(`DB 연결 실패: ${error.message}\n DATABASE_URL을 확인해 주세요 (지금: ${DATABASE_URL})`);
  process.exit(1);
}
const tokenA = accessToken(aId);
const tokenB = accessToken(bId);

let a, b;
try {
  a = await connect(tokenA);
  b = await connect(tokenB);
  check("A·B 로그인 쿠키로 WebSocket 연결", true);
} catch (error) {
  check("A·B 로그인 쿠키로 WebSocket 연결", false,
    `${error.message}. 백엔드 실행 여부, JWT_SECRET, ORIGIN(=FRONTEND_BASE_URL)을 확인해 주세요`);
  process.exit(1);
}

const text = `dm-check ${new Date().toISOString()}`;
a.client.publish({ destination: "/app/dm.send", body: JSON.stringify({ receiverId: bId, content: text }) });
await wait(800);
check("B가 A의 메시지를 실시간으로 받음", b.inbox.some((m) => m.content === text && m.senderId === aId));
check("A(보낸 사람)에게도 같은 메시지가 옴", a.inbox.some((m) => m.content === text));

let res = await get("/api/v1/dm/rooms", tokenB);
const roomOfB = res.body?.data?.find((room) => room.user.id === aId);
check("B 대화방 목록에 A와의 방이 있음", res.status === 200 && roomOfB?.lastMessage === text,
  `status ${res.status}, 마지막 메시지 "${roomOfB?.lastMessage}"`);
check("B의 안 읽은 메시지 수가 1 이상", (roomOfB?.unreadCount ?? 0) >= 1, `unreadCount ${roomOfB?.unreadCount}`);

res = await get(`/api/v1/dm/${aId}`, tokenB);
const messages = res.body?.data?.content ?? [];
check("대화 내역의 마지막이 방금 보낸 메시지(오래된 → 최신 순)", messages.at(-1)?.content === text,
  `${messages.length}건`);

res = await get("/api/v1/dm/rooms", tokenB);
check("대화를 연 뒤 B의 안 읽은 수가 0",
  res.body?.data?.find((room) => room.user.id === aId)?.unreadCount === 0);

res = await get(`/api/v1/dm/${bId}`, tokenA);
check("A가 보낸 메시지가 '읽음'으로 표시됨", res.body?.data?.content?.at(-1)?.read === true);

a.client.publish({ destination: "/app/dm.send", body: JSON.stringify({ receiverId: bId, content: "   " }) });
await wait(800);
check("빈 메시지는 보낸 사람에게만 오류가 감", a.errors.length > 0 && b.errors.length === 0,
  a.errors.at(-1)?.message ?? "");

res = await get("/api/v1/dm/rooms", null);
check("로그인 없이 목록을 부르면 401", res.status === 401, `status ${res.status}`);

await a.client.deactivate();
await b.client.deactivate();

console.log(failed === 0 ? "\n모두 통과했어요." : `\n${failed}개 실패했어요.`);
process.exit(failed === 0 ? 0 : 1);
