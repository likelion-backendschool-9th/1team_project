// 로컬 전용. seed.sql로 넣은 더미 데이터를 지운다.
import fs from "node:fs";
import pg from "pg";

const DATABASE_URL =
  process.env.DATABASE_URL ?? "postgres://dipsource:dipsource@localhost:5000/dipsource";

const db = new pg.Client({ connectionString: DATABASE_URL });
await db.connect();
await db.query(fs.readFileSync(new URL("./cleanup.sql", import.meta.url), "utf8"));
await db.end();
console.log("cleanup 완료");
