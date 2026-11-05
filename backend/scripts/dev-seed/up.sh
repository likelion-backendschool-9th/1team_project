#!/usr/bin/env bash
# 로컬 전용. 시드 넣고 팀원 5명 access_token을 출력한다.
# 사용: 저장소 루트 또는 이 폴더에서 ./up.sh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../../.." && pwd)"
DIR="$(cd "$(dirname "$0")" && pwd)"

if [[ -f "$ROOT/.env" ]]; then
  set -a
  # shellcheck disable=SC1091
  source "$ROOT/.env"
  set +a
fi

DB_URL="${DATABASE_URL:-postgres://${POSTGRES_USER:-dipsource}:${POSTGRES_PASSWORD:-dipsource}@localhost:5000/${POSTGRES_DB:-dipsource}}"
JWT_SECRET="${JWT_SECRET:?ROOT .env에 JWT_SECRET이 필요해요}"

echo "→ seed.sql"
psql "$DB_URL" -v ON_ERROR_STOP=1 -f "$DIR/seed.sql"

echo ""
echo "→ 팀원 토큰 (쿠키 access_token에 붙여넣기)"
psql "$DB_URL" -At -c \
  "select id || '|' || nickname || '|' || email from users where email like 'seed-team-%@dev.local' order by email" \
| while IFS='|' read -r id nickname email; do
  token="$(
    JWT_SECRET="$JWT_SECRET" USER_ID="$id" node <<'NODE'
const crypto = require("crypto");
const secret = process.env.JWT_SECRET;
const userId = process.env.USER_ID;
const enc = (v) => Buffer.from(JSON.stringify(v)).toString("base64url");
const now = Math.floor(Date.now() / 1000);
const body = `${enc({ alg: "HS256" })}.${enc({ sub: String(userId), iat: now, exp: now + 86400, role: "USER" })}`;
const sig = crypto.createHmac("sha256", secret).update(body).digest("base64url");
process.stdout.write(`${body}.${sig}`);
NODE
  )"
  echo ""
  echo "$nickname <$email> (id $id)"
  echo "  $token"
done

echo ""
echo "프론트 .env.local: API_ORIGIN=http://localhost:8080  (MOCK_EXCEPT_DM 끄기)"
echo "지울 때: ./cleanup.sh"
