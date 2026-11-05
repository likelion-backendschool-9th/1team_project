// 로컬 전용. 시드 회원을 넣고(이미 있으면 건너뜀) 팀원 5명 로그인 토큰을 출력한다.
// CI/CD·배포 서버에서는 실행하지 않는다.
//
// 실행: JWT_SECRET=<백엔드와 같은 값> npm run preview
import crypto from "node:crypto";
import fs from "node:fs";
import pg from "pg";

const DATABASE_URL =
  process.env.DATABASE_URL ?? "postgres://dipsource:dipsource@localhost:5000/dipsource";
const JWT_SECRET = process.env.JWT_SECRET;

if (!JWT_SECRET) {
  console.error("JWT_SECRET이 필요해요. 백엔드를 띄울 때 쓴 값과 같아야 합니다.");
  process.exit(1);
}

// 백엔드 JwtProvider와 같은 형식(HS256, sub = 회원 ID). 확인용이라 24시간 유효하다.
function accessToken(userId) {
  const encode = (value) => Buffer.from(JSON.stringify(value)).toString("base64url");
  const now = Math.floor(Date.now() / 1000);
  const body = `${encode({ alg: "HS256" })}.${encode({
    sub: String(userId),
    iat: now,
    exp: now + 86400,
    role: "USER",
  })}`;
  const signature = crypto.createHmac("sha256", JWT_SECRET).update(body).digest("base64url");
  return `${body}.${signature}`;
}

const db = new pg.Client({ connectionString: DATABASE_URL });
await db.connect();
await db.query(fs.readFileSync(new URL("./seed.sql", import.meta.url), "utf8"));

const { rows: teams } = await db.query(
  `select id, nickname, email
   from users
   where email like 'seed-team-%@dev.local'
   order by email`,
);

const { rows: counts } = await db.query(
  `select
     (select count(*)::int from users where email like 'seed-%@dev.local') as users,
     (select count(*)::int from mini_homes mh
        join users u on u.id = mh.user_id where u.email like 'seed-%@dev.local') as mini_homes,
     (select count(*)::int from feeds f
        join users u on u.id = f.user_id where u.email like 'seed-%@dev.local') as feeds,
     (select count(*)::int from posts p
        join users u on u.id = p.user_id where u.email like 'seed-%@dev.local') as posts,
     (select count(*)::int from demos d
        join users u on u.id = d.user_id where u.email like 'seed-%@dev.local') as demos`,
);

await db.end();

const c = counts[0];
console.log("시드 요약");
console.log(`  users=${c.users} mini_homes=${c.mini_homes} feeds=${c.feeds} posts=${c.posts} demos=${c.demos}`);
console.log("");
console.log("팀원 5명 토큰 (브라우저 쿠키 access_token에 넣으면 그 회원으로 로그인돼요)\n");

for (const { id, nickname, email } of teams) {
  console.log(`${nickname} <${email}> (id ${id})`);
  console.log(`  ${accessToken(id)}\n`);
}

console.log("지우려면: npm run cleanup");
console.log("또는: psql \"$DATABASE_URL\" -f cleanup.sql");
