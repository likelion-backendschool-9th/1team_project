#!/usr/bin/env bash
# 통합 검색 부하 테스트를 시나리오별로 돌리고 결과를 모은다. 로컬에서만 쓴다.
#
# 준비
#   1. DB: pg_stat_statements 켜기 (pg-stat.sql 맨 위 설명), 데이터 넣기 (load-seed.sql)
#   2. 서버: metrics 주소를 열고 띄운다
#        MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,metrics SERVER_PORT=8081 ./gradlew bootRun
#   3. brew install k6 jq
# 실행
#   ./load-test.sh 10k            # 결과: results/10k/
#   ./load-test.sh 10k s1         # 시나리오 하나만 (s1 / s2 / s3)
#
# s1(기준선)은 요청 종류마다 따로 돌리고, 그동안 쌓인 서버 측정값(search.query)의 차이로
# "요청 한 번에 어떤 쿼리 메서드가 몇 번, 평균 몇 ms" 걸렸는지 계산한다.
set -euo pipefail

LABEL=${1:?결과 이름이 필요해요. 예: ./load-test.sh 10k}
ONLY=${2:-all}
BASE_URL=${BASE_URL:-http://localhost:8081}
DB_CONTAINER=${DB_CONTAINER:-dipsource-db-local}
ITERATIONS=${ITERATIONS:-30} # s1 요청 종류마다 잴 횟수. 데이터가 커서 오래 걸리면 줄인다
DIR=$(cd "$(dirname "$0")" && pwd)
OUT="$DIR/results/$LABEL"
mkdir -p "$OUT"

psql_db() {
  docker exec -i "$DB_CONTAINER" sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -q -P pager=off "$@"' -- "$@"
}

metric() { # metric 이름 [tag=값 ...] → 측정값 JSON (없으면 빈 값)
  local name=$1; shift
  local query=""
  for tag in "$@"; do query+="&tag=${tag/=/:}"; done
  curl -sf "$BASE_URL/actuator/metrics/$name?${query#&}" || true
}

stat() { # metric JSON에서 COUNT·TOTAL_TIME·VALUE 같은 값 하나
  jq -r --arg s "$2" '[.measurements[]? | select(.statistic == $s) | .value][0] // 0' <<<"$1"
}

snapshot() { # search.query의 class·method별 "class method count total_seconds"
  local tags classes methods
  tags=$(metric search.query)
  classes=$(jq -r '.availableTags[]? | select(.tag == "class") | .values[]' <<<"$tags")
  methods=$(jq -r '.availableTags[]? | select(.tag == "method") | .values[]' <<<"$tags")
  for class in $classes; do
    for method in $methods; do
      local m; m=$(metric search.query "class=$class" "method=$method")
      [ -n "$m" ] && echo "${class##*.} $method $(stat "$m" COUNT) $(stat "$m" TOTAL_TIME)"
    done
  done
  return 0
}

k6_summary() { # k6 요약 JSON → 표 한 줄씩
  jq -r '
    def ms: if . == null then "-" else (. * 100 | round / 100 | tostring) end;
    .metrics as $m
    | ["search_latency", "autocomplete_latency"][]
    | select($m[.] != null and $m[.].count > 0) as $k
    | "\($k)\tcount=\($m[$k].count)\tavg=\($m[$k].avg | ms)\tp50=\($m[$k].med | ms)\tp95=\($m[$k]["p(95)"] | ms)\tp99=\($m[$k]["p(99)"] | ms)\tmax=\($m[$k].max | ms)"
  ' "$1"
  jq -r '"rps=\(.metrics.http_reqs.rate * 100 | round / 100)\tfailed=\(.metrics.http_req_failed.value * 10000 | round / 100)%"' "$1"
}

run_k6() { # run_k6 결과이름 k6 -e 옵션...
  local name=$1; shift
  k6 run --quiet --summary-export "$OUT/$name.json" "$@" "$DIR/load.js" > "$OUT/$name.k6.txt" 2>&1 || true
  # 기준(threshold) 초과는 결과로 보고, 그 밖의 k6 오류(데우기 시간 초과 등)는 그대로 보여 준다
  grep 'level=error' "$OUT/$name.k6.txt" | grep -v 'thresholds on metrics' || true
  k6_summary "$OUT/$name.json"
}

hikari_watch() { # 부하 동안 1초마다 커넥션 풀 상태를 적는다: 시각 active pending
  while true; do
    echo "$(date +%s) $(stat "$(metric hikaricp.connections.active)" VALUE) $(stat "$(metric hikaricp.connections.pending)" VALUE)"
    sleep 1
  done
}

[ -n "$(metric search.query)" ] || { echo "metrics 주소가 닫혀 있거나 서버가 없어요: $BASE_URL"; exit 1; }

{
  echo "# $LABEL ($(date '+%Y-%m-%d %H:%M'))"
  psql_db -At -F ' ' -c "SELECT 'data users=' || (SELECT count(*) FROM users) || ' posts=' || (SELECT count(*) FROM posts)
                                || ' feeds=' || (SELECT count(*) FROM feeds) || ' demos=' || (SELECT count(*) FROM demos)
                                || ' tags=' || (SELECT count(*) FROM tags)"

  if [ "$ONLY" = all ] || [ "$ONLY" = s1 ]; then
    echo; echo "## S1 기준선 (사용자 1명, 요청 ${ITERATIONS}번, 그 전에 데우기 3번은 결과에서 뺌)"
    for case in hit multi typo few ac acKo; do
      psql_db -c "SELECT pg_stat_statements_reset()" > /dev/null
      snapshot > "$OUT/s1-$case.before"
      echo "### $case"
      run_k6 "s1-$case" -e SCENARIO=s1 -e CASE="$case" -e ITERATIONS="$ITERATIONS" -e BASE_URL="$BASE_URL"
      snapshot > "$OUT/s1-$case.after"
      # 데우기 3번 + 측정 횟수만큼의 요청 동안 쌓인 차이 → 요청 한 번 기준
      awk -v n=$((ITERATIONS + 3)) 'NR == FNR {count[$1"."$2] = $3; total[$1"."$2] = $4; next}
           {key = $1"."$2; calls = $3 - count[key]; secs = $4 - total[key]
            if (calls > 0) printf "  %-40s calls/req=%.1f mean=%.2fms total/req=%.2fms\n", key, calls / n, secs * 1000 / calls, secs * 1000 / n}' \
        "$OUT/s1-$case.before" "$OUT/s1-$case.after"
      psql_db < "$DIR/pg-stat.sql" > "$OUT/s1-$case.pg.txt"
    done
  fi

  for s in s2 s3; do
    if [ "$ONLY" = all ] || [ "$ONLY" = "$s" ]; then
      echo
      [ "$s" = s2 ] && echo "## S2 타이핑 (사용자 10명, 60초, 글자당 0.3초 → 자동완성, 다 치면 검색)"
      [ "$s" = s3 ] && echo "## S3 동시 사용자 증가 (1 → 10 → 50명, 검색 후 1초 쉼)"
      psql_db -c "SELECT pg_stat_statements_reset()" > /dev/null
      acquire_before=$(metric hikaricp.connections.acquire)
      hikari_watch > "$OUT/$s.hikari.txt" & watcher=$!
      run_k6 "$s" -e SCENARIO="$s" -e BASE_URL="$BASE_URL"
      kill "$watcher" 2>/dev/null; wait "$watcher" 2>/dev/null || true
      acquire_after=$(metric hikaricp.connections.acquire)
      awk '{if ($2 > a) a = $2; if ($3 > p) p = $3} END {printf "hikari max_active=%d max_pending=%d\n", a, p}' "$OUT/$s.hikari.txt"
      awk -v c1="$(stat "$acquire_before" COUNT)" -v t1="$(stat "$acquire_before" TOTAL_TIME)" \
          -v c2="$(stat "$acquire_after" COUNT)" -v t2="$(stat "$acquire_after" TOTAL_TIME)" \
          'BEGIN {if (c2 > c1) printf "hikari acquire count=%d mean_wait=%.2fms\n", c2 - c1, (t2 - t1) * 1000 / (c2 - c1)}'
      echo "top queries:"
      psql_db < "$DIR/pg-stat.sql" | tee "$OUT/$s.pg.txt" | sed -n '3,8p'
    fi
  done
} | tee "$OUT/summary.txt"
