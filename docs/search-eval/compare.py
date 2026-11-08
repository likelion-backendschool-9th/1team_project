#!/usr/bin/env python3
"""검색 평가 결과 두 개를 비교한다. 사용: compare.py <before.json> <after.json>"""
import json
import sys


def load(path):
    with open(path, encoding="utf-8") as f:
        return json.load(f)


def main(before_path, after_path):
    before, after = load(before_path), load(after_path)
    print("| 지표 | {} | {} | 차이 |".format(before["summary"]["profile"], after["summary"]["profile"]))
    print("|---|---|---|---|")
    for key in ("meanP10", "mrr", "meanNdcg10", "suggestionAccuracy"):
        b, a = before["summary"][key], after["summary"][key]
        print("| {} | {} | {} | {:+.4f} |".format(key, b, a, a - b))

    b_rows = {r["id"]: r for r in before["results"]}
    improved, regressed = [], []
    for row in after["results"]:
        prev = b_rows.get(row["id"], {})
        if "ndcg10" not in row or "ndcg10" not in prev:
            continue
        delta = row["ndcg10"] - prev["ndcg10"]
        if delta > 0.001:
            improved.append((delta, row, prev))
        elif delta < -0.001:
            regressed.append((delta, row, prev))

    for title, items in (("개선", sorted(improved, key=lambda x: -x[0])), ("악화", sorted(regressed, key=lambda x: x[0]))):
        print("\n**{} {}건**\n".format(title, len(items)))
        for delta, row, prev in items:
            print("- `{}` ({}) NDCG {} → {} ({:+.3f})".format(row["q"], row["type"], prev["ndcg10"], row["ndcg10"], delta))
            print("  - 이전: {}".format(", ".join(prev["top"][:5]) or "(없음)"))
            print("  - 이후: {}".format(", ".join(row["top"][:5]) or "(없음)"))


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2])
