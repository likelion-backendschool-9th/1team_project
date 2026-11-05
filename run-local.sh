#!/usr/bin/env bash
# 백엔드(Spring Boot) + 프론트엔드(Next.js)를 동시에 실행하는 스크립트
# 종료: Ctrl+C 한 번이면 둘 다 꺼짐

set -e

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"

cleanup() {
  echo ""
  echo "서버 종료 중..."
  kill $BE_PID $FE_PID 2>/dev/null
  wait $BE_PID $FE_PID 2>/dev/null
  echo "완료"
}
trap cleanup EXIT INT TERM

# 백엔드
echo "▶ 백엔드 시작 (http://localhost:8080)"
cd "$ROOT_DIR/backend"
./gradlew bootRun &
BE_PID=$!

# 프론트엔드
echo "▶ 프론트엔드 시작 (http://localhost:3000)"
cd "$ROOT_DIR/frontend"
npm run dev &
FE_PID=$!

wait
