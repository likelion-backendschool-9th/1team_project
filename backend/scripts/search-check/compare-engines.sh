#!/usr/bin/env bash
# PostgreSQL 경로와 Elasticsearch 경로를 같은 머신·데이터·설정에서 비교한다. 로컬에서만 쓴다.
#
#   ./compare-engines.sh setup          # 전용 컨테이너(55432, 59200) 생성, 글 10만 건 시드, 색인
#   ./compare-engines.sh run            # 사용자 1·10·50·100명, 엔진 순서를 바꿔 2회 측정. 결과: results/compare/
#   ./compare-engines.sh report         # 결과 요약(Markdown)
#   ./compare-engines.sh outage         # Elasticsearch를 실제로 멈췄다가 복구하는 시나리오
#   ./compare-engines.sh teardown       # 전용 컨테이너 삭제
#
# 동일 조건: 같은 jar, JVM 힙 1GB 고정, 두 컨테이너 모두 실행, 색인·동기화 구성요소도 양쪽 모두 켬(읽기 경로만 다름),
# 측정 전 같은 검색어로 데우기, 첫 페이지만 요청(재정렬 캐시는 첫 페이지에 쓰이지 않음).
# 필요: Docker, k6, jq, JDK 21. 이미지: docker build -t dipsource-elasticsearch:8.15.5 ../../../docker/elasticsearch
set -euo pipefail

DIR=$(cd "$(dirname "$0")" && pwd)
BACKEND=$(cd "$DIR/../.." && pwd)
OUT="$DIR/results/compare"
JAR="$BACKEND/build/libs/dipsource-backend-0.0.1-SNAPSHOT.jar"
DB=search-perf-db
ES=search-perf-es
PORT=8081
VUS_LEVELS=${VUS_LEVELS:-"1 10 50 100"}
DURATION=${DURATION:-60s}
ROUNDS=${ROUNDS:-2}

backend_env() {
  export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:55432/dipsource
  export SPRING_DATASOURCE_USERNAME=dipsource SPRING_DATASOURCE_PASSWORD=dipsource
  export JWT_SECRET=perf-secret-perf-secret-perf-secret-perf-secret-0000
  export GITHUB_CLIENT_ID=x GITHUB_CLIENT_SECRET=x
  export SERVER_PORT=$PORT MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,metrics
  export SPRING_ELASTICSEARCH_URIS=http://localhost:59200
  export SEARCH_ES_ENABLED=true DEV_LOGIN_ENABLED=true
}

start_backend() { # start_backend 엔진 로그파일
  backend_env
  SEARCH_ENGINE=$1 java -Xms1g -Xmx1g -jar "$JAR" > "$2" 2>&1 &
  BACKEND_PID=$!
  until curl -sf "localhost:$PORT/api/v1/actuator/health" > /dev/null; do sleep 1; done
}

stop_backend() {
  kill "$BACKEND_PID" 2> /dev/null || true
  wait "$BACKEND_PID" 2> /dev/null || true
}

setup() {
  docker rm -f "$DB" "$ES" > /dev/null 2>&1 || true
  docker run -d --name "$DB" -e POSTGRES_DB=dipsource -e POSTGRES_USER=dipsource -e POSTGRES_PASSWORD=dipsource \
    -p 55432:5432 postgres:16 -c shared_preload_libraries=pg_stat_statements > /dev/null
  docker run -d --name "$ES" -e discovery.type=single-node -e xpack.security.enabled=false \
    -e ES_JAVA_OPTS="-Xms1g -Xmx1g" -p 59200:9200 dipsource-elasticsearch:8.15.5 > /dev/null
  until docker exec "$DB" pg_isready -U dipsource > /dev/null 2>&1; do sleep 1; done
  until curl -sf localhost:59200/_cluster/health > /dev/null; do sleep 2; done
  (cd "$BACKEND" && ./gradlew bootJar -q -x test)
  # 스키마를 만들려고 한 번 띄운다. 이때는 색인 구성요소를 끈다
  backend_env; export SEARCH_ES_ENABLED=false
  SEARCH_ENGINE=postgres java -jar "$JAR" > "$OUT/boot.log" 2>&1 &
  BACKEND_PID=$!
  until curl -sf "localhost:$PORT/api/v1/actuator/health" > /dev/null; do sleep 1; done
  stop_backend
  docker exec -i "$DB" psql -U dipsource -d dipsource -q < "$DIR/seed.sql"
  docker exec -i "$DB" sh -c 'psql -U dipsource -d dipsource -q -v posts=100000' < "$DIR/load-seed.sql"
  # 비어 있는 인덱스를 자동으로 채운다
  start_backend elasticsearch "$OUT/index.log"
  local started=$SECONDS
  until [ "$(curl -s localhost:59200/dipsource_feed/_count | jq -r '.count // 0')" -gt 90000 ] \
     && [ "$(curl -s localhost:59200/dipsource_suggest/_count | jq -r '.count // 0')" -gt 0 ]; do sleep 2; done
  echo "초기 색인 $((SECONDS - started))초"
  stop_backend
}

sampler() { # 2초마다: 시각 PG_CPU PG_MEM ES_CPU ES_MEM JVM_CPU JVM_RSS_MB HIKARI_PENDING
  while true; do
    local stats pg es jvm pending
    stats=$(docker stats --no-stream --format '{{.Name}} {{.CPUPerc}} {{.MemUsage}}' "$DB" "$ES")
    pg=$(awk -v n="$DB" '$1 == n {gsub("%", "", $2); print $2, $3}' <<<"$stats")
    es=$(awk -v n="$ES" '$1 == n {gsub("%", "", $2); print $2, $3}' <<<"$stats")
    jvm=$(ps -o %cpu=,rss= -p "$BACKEND_PID" | awk '{printf "%s %d", $1, $2 / 1024}')
    pending=$(curl -s "localhost:$PORT/api/v1/actuator/metrics/hikaricp.connections.pending" | jq -r '.measurements[0].value // 0')
    echo "$(date +%s) $pg $es $jvm $pending"
    sleep 2
  done
}

measure() { # measure 라운드 엔진
  local round=$1 engine=$2
  start_backend "$engine" "$OUT/r$round-$engine.log"
  # 같은 검색어로 데운다
  for _ in 1 2 3; do
    for q in spring react docker 'spring%20jpa' 'kafka%20%EC%84%B1%EB%8A%A5' '%EC%BA%90%EC%8B%9C' sprnig sprng; do
      curl -s -o /dev/null --max-time 60 "localhost:$PORT/api/v1/search?q=$q"
    done
  done
  for vus in $VUS_LEVELS; do
    sampler > "$OUT/r$round-$engine-$vus.res.txt" &
    local sampler_pid=$!
    k6 run --quiet --summary-export "$OUT/r$round-$engine-$vus.json" \
      -e VUS="$vus" -e DURATION="$DURATION" -e BASE_URL="http://localhost:$PORT" "$DIR/compare.js" \
      > "$OUT/r$round-$engine-$vus.k6.txt" 2>&1 || true
    kill "$sampler_pid" 2> /dev/null; wait "$sampler_pid" 2> /dev/null || true
    echo "완료: round=$round engine=$engine vus=$vus"
    sleep 5
  done
  stop_backend
}

run() {
  for round in $(seq 1 "$ROUNDS"); do
    # 시간에 따른 머신 상태 변화를 상쇄하려고 라운드마다 순서를 바꾼다
    if [ $((round % 2)) -eq 1 ]; then order="postgres elasticsearch"; else order="elasticsearch postgres"; fi
    for engine in $order; do measure "$round" "$engine"; done
  done
}

mkdir -p "$OUT"
case "${1:-}" in
  setup) setup ;;
  run) run ;;
  report) python3 "$DIR/compare-report.py" "$OUT" ;;
  outage)
    start_backend elasticsearch "$OUT/outage.log"
    python3 "$DIR/outage-recovery.py" "${2:-$OUT/outage-recovery-result.json}" || true
    stop_backend
    ;;
  teardown) docker rm -f "$DB" "$ES" ;;
  *) echo "사용: $0 setup|run|report|outage|teardown"; exit 1 ;;
esac
