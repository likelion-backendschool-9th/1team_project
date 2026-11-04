// DM 화면을 눈으로 확인하기 위한 준비. (docs/임시-목업-현황.md)
// 더미 회원·대화를 넣고(이미 있으면 건너뜀), 브라우저에 붙여 넣을 로그인 토큰을 출력한다.
//
// 실행: JWT_SECRET=<백엔드와 같은 값> npm run preview
import crypto from "node:crypto";
import fs from "node:fs";
import pg from "pg";

const DATABASE_URL = process.env.DATABASE_URL ?? "postgres://dipsource:dipsource@localhost:5432/dipsource";
const JWT_SECRET = process.env.JWT_SECRET;

if (!JWT_SECRET) {
  console.error("JWT_SECRET이 필요해요. 백엔드를 띄울 때 쓴 값과 같아야 합니다.");
  process.exit(1);
}

// 백엔드 JwtProvider와 같은 형식(HS256, sub = 회원 ID). 확인용이라 24시간 유효하다.
function accessToken(userId) {
  const encode = (value) => Buffer.from(JSON.stringify(value)).toString("base64url");
  const now = Math.floor(Date.now() / 1000);
  const body = `${encode({ alg: "HS256" })}.${encode({ sub: String(userId), iat: now, exp: now + 86400, role: "USER" })}`;
  const signature = crypto.createHmac("sha256", JWT_SECRET).update(body).digest("base64url");
  return `${body}.${signature}`;
}

const db = new pg.Client({ connectionString: DATABASE_URL });
await db.connect();
await db.query(fs.readFileSync(new URL("./preview-seed.sql", import.meta.url), "utf8"));
const { rows } = await db.query(
  "select id, nickname from users where email like 'preview-%@test.local' order by id",
);
await db.end();

console.log("더미 회원 (브라우저 쿠키 access_token에 토큰을 넣으면 그 회원으로 로그인돼요)\n");
for (const { id, nickname } of rows) {
  console.log(`${nickname} (id ${id})\n  ${accessToken(id)}\n`);
}
console.log("지우려면: psql ... -f preview-cleanup.sql");
