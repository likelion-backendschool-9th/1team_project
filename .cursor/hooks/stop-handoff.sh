#!/usr/bin/env bash
# stop: preCompact가 남긴 마커가 있으면 에이전트에게 handoff.md 갱신 followup
set -euo pipefail

HOOKS="$(cd "$(dirname "$0")" && pwd)"
MARKER="$HOOKS/.pending-handoff"

if [[ ! -f "$MARKER" ]]; then
  echo '{}'
  exit 0
fi

rm -f "$MARKER"

python3 -c '
import json
msg = (
  "컨텍스트 압축이 방금 일어났거나 예정되어 있었습니다. "
  "지금 바로 docs/_local/handoff.md 를 현재 작업 상태"
  "(브랜치, 기능 단계, 파일 리뷰 몇/몇, 한 일, 다음 할 일, 열어볼 파일)로 갱신하세요. "
  "docs/_local/handoff-compact-snapshot.md 가 있으면 git 상태는 그걸 참고하세요. "
  "사용자에게는 한 줄로 handoff 갱신했다고만 알리세요."
)
print(json.dumps({"followup_message": msg}, ensure_ascii=False))
'
exit 0
