#!/usr/bin/env bash
# 로컬 전용. seed 데이터 삭제.
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

psql "$DB_URL" -v ON_ERROR_STOP=1 -f "$DIR/cleanup.sql"
echo "cleanup 완료"
