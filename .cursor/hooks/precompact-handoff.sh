#!/usr/bin/env bash
# preCompact: 컨텍스트 압축 직전 — 기계적 스냅샷 저장 + stop 훅이 핸드오프 갱신을 요청하도록 마커 생성
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
LOCAL="$ROOT/docs/_local"
HOOKS="$(cd "$(dirname "$0")" && pwd)"
mkdir -p "$LOCAL"

input="$(cat)"

# python3로 stdin JSON 파싱 (jq 없이도 동작)
meta="$(python3 -c '
import json,sys
d=json.load(sys.stdin)
print(d.get("trigger",""))
print(d.get("context_usage_percent",""))
print(d.get("context_tokens",""))
print(d.get("context_window_size",""))
print(d.get("message_count",""))
print(d.get("messages_to_compact",""))
print(d.get("is_first_compaction",""))
' <<<"$input" 2>/dev/null || true)"

trigger="$(echo "$meta" | sed -n '1p')"
usage="$(echo "$meta" | sed -n '2p')"
tokens="$(echo "$meta" | sed -n '3p')"
window="$(echo "$meta" | sed -n '4p')"
msgs="$(echo "$meta" | sed -n '5p')"
to_compact="$(echo "$meta" | sed -n '6p')"
first="$(echo "$meta" | sed -n '7p')"

branch="$(git -C "$ROOT" branch --show-current 2>/dev/null || echo unknown)"
status="$(git -C "$ROOT" status -sb 2>/dev/null || true)"
diffstat="$(git -C "$ROOT" diff --stat HEAD 2>/dev/null | tail -n 20 || true)"

ts="$(date '+%Y-%m-%d %H:%M:%S %z')"

cat > "$LOCAL/handoff-compact-snapshot.md" <<EOF
# Compact 직전 자동 스냅샷

- **시각:** $ts
- **trigger:** ${trigger:-?}
- **context 사용:** ${usage:-?}% (${tokens:-?} / ${window:-?} tokens)
- **메시지:** ${msgs:-?}개 중 압축 예정 ${to_compact:-?}개
- **첫 compact:** ${first:-?}
- **브랜치:** $branch

## git status

\`\`\`
$status
\`\`\`

## diff --stat

\`\`\`
$diffstat
\`\`\`

> 이 파일은 기계적 스냅샷임. 작업 의미는 \`handoff.md\`를 에이전트가 갱신해야 함.
EOF

# stop 훅이 followup으로 핸드오프 갱신을 요청하게 함
printf '%s\n' "$ts" > "$HOOKS/.pending-handoff"

# observational only — user_message만 가능
python3 -c '
import json
msg = (
  "컨텍스트가 거의 꽉 차서 압축됩니다. "
  "docs/_local/handoff-compact-snapshot.md 에 스냅샷을 저장했고, "
  "다음 에이전트 턴에서 handoff.md 갱신을 요청합니다. "
  "Claude로 넘길 때는 docs/_local/handoff.md 를 열어 주세요."
)
print(json.dumps({"user_message": msg}, ensure_ascii=False))
'
exit 0
