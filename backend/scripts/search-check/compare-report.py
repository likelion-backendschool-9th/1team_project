#!/usr/bin/env python3
"""compare-engines.sh 결과를 Markdown 표로 만든다. 사용: compare-report.py <결과 폴더>"""
import glob
import json
import pathlib
import re
import statistics
import sys


def mem_mb(text):
    match = re.match(r"([\d.]+)([KMG]i?B)", text)
    value, unit = float(match.group(1)), match.group(2)
    return value * {"KiB": 1 / 1024, "MiB": 1, "GiB": 1024, "KB": 1 / 1024, "MB": 1, "GB": 1024}[unit]


def resources(path):
    columns = {"pg_cpu": [], "pg_mem": [], "es_cpu": [], "es_mem": [], "jvm_cpu": [], "jvm_mem": [], "pending": []}
    for line in pathlib.Path(path).read_text().splitlines():
        parts = line.split()
        if len(parts) != 8:
            continue
        columns["pg_cpu"].append(float(parts[1]))
        columns["pg_mem"].append(mem_mb(parts[2]))
        columns["es_cpu"].append(float(parts[3]))
        columns["es_mem"].append(mem_mb(parts[4]))
        columns["jvm_cpu"].append(float(parts[5]))
        columns["jvm_mem"].append(float(parts[6]))
        columns["pending"].append(float(parts[7]))
    return columns


def main(folder):
    rows = []
    for file in sorted(glob.glob(f"{folder}/r*-*-*.json")):
        match = re.search(r"r(\d+)-(\w+)-(\d+)\.json$", file)
        if not match:
            continue
        round_no, engine, vus = int(match.group(1)), match.group(2), int(match.group(3))
        metrics = json.load(open(file, encoding="utf-8"))["metrics"]
        latency = metrics["search_latency"]
        res = resources(file.replace(".json", ".res.txt"))
        rows.append({
            "round": round_no, "engine": engine, "vus": vus,
            "p50": latency["med"], "p95": latency["p(95)"], "p99": latency["p(99)"], "max": latency["max"],
            "count": latency["count"], "rps": metrics["http_reqs"]["rate"],
            "error": metrics["http_req_failed"]["value"] * 100,
            "pg_cpu": statistics.mean(res["pg_cpu"]), "es_cpu": statistics.mean(res["es_cpu"]),
            "jvm_cpu": statistics.mean(res["jvm_cpu"]),
            "pg_mem": max(res["pg_mem"]), "es_mem": max(res["es_mem"]), "jvm_mem": max(res["jvm_mem"]),
            "pending": max(res["pending"]),
        })

    print("| 사용자 | 엔진 | 회차 | p50 ms | p95 ms | p99 ms | 최대 ms | RPS | 오류 % | PG CPU % | ES CPU % | JVM CPU % | PG MB | ES MB | JVM MB | 풀 대기 최대 |")
    print("|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|")
    for r in sorted(rows, key=lambda x: (x["vus"], x["engine"], x["round"])):
        print(f"| {r['vus']} | {r['engine']} | {r['round']} | {r['p50']:.0f} | {r['p95']:.0f} | {r['p99']:.0f} | {r['max']:.0f} | "
              f"{r['rps']:.1f} | {r['error']:.2f} | {r['pg_cpu']:.0f} | {r['es_cpu']:.0f} | {r['jvm_cpu']:.0f} | "
              f"{r['pg_mem']:.0f} | {r['es_mem']:.0f} | {r['jvm_mem']:.0f} | {r['pending']:.0f} |")

    print("\n2회 평균\n")
    print("| 사용자 | 엔진 | p50 ms | p95 ms | p99 ms | RPS | 오류 % |")
    print("|---|---|---|---|---|---|---|")
    groups = {}
    for r in rows:
        groups.setdefault((r["vus"], r["engine"]), []).append(r)
    for (vus, engine), items in sorted(groups.items()):
        avg = lambda key: statistics.mean(i[key] for i in items)
        print(f"| {vus} | {engine} | {avg('p50'):.0f} | {avg('p95'):.0f} | {avg('p99'):.0f} | {avg('rps'):.1f} | {avg('error'):.2f} |")


if __name__ == "__main__":
    main(sys.argv[1])
