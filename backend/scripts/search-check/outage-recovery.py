#!/usr/bin/env python3
"""Elasticsearch를 실제로 멈췄다가 복구했을 때 검색 동작과 색인 누락을 확인한다. compare-engines.sh outage로 실행한다."""
import json
import os
import subprocess
import sys
import time
import urllib.error
import urllib.parse
import urllib.request

BASE = "http://localhost:8081"
ES = "http://localhost:59200"
DB = "search-perf-db"
ES_CONTAINER = "search-perf-es"
results = []


def record(name, expected, actual, passed):
    results.append({"항목": name, "예상": expected, "실제": actual, "통과": bool(passed)})
    print(("통과 " if passed else "실패 ") + name + f" | 예상: {expected} | 실제: {actual}", flush=True)


def sql(statement):
    out = subprocess.run(["docker", "exec", DB, "psql", "-U", "dipsource", "-d", "dipsource", "-Atc", statement],
                         capture_output=True, text=True, check=True).stdout
    return out.strip()


def request(path, method="GET", body=None, token=None, timeout=30):
    data = None if body is None else json.dumps(body).encode()
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Cookie"] = "access_token=" + token
    req = urllib.request.Request(BASE + path, data=data, method=method, headers=headers)
    started = time.time()
    try:
        with urllib.request.urlopen(req, timeout=timeout) as res:
            raw = res.read().decode()
            return res.status, json.loads(raw) if raw else None, time.time() - started
    except urllib.error.HTTPError as err:
        return err.code, None, time.time() - started


def login(email):
    return request("/api/v1/auth/dev-login", "POST", {"email": email})[1]["data"]["accessToken"]


def feed_total(word):
    status, body, seconds = request("/api/v1/search?type=feed&q=" + urllib.parse.quote(word))
    return status, (body["data"]["totalElements"] if body else None), seconds


def wait_until(check, timeout):
    started = time.time()
    while time.time() - started < timeout:
        if check():
            return time.time() - started
        time.sleep(0.25)
    return None


def es_doc_exists(feed_id):
    alias = json.load(urllib.request.urlopen(f"{ES}/_alias/dipsource_feed"))
    index = next(iter(alias))
    try:
        return json.load(urllib.request.urlopen(f"{ES}/{index}/_doc/{feed_id}")).get("found", False)
    except urllib.error.HTTPError:
        return False


def docker(*args):
    subprocess.run(["docker", *args], check=True, capture_output=True)


def main():
    sql("UPDATE users SET role = 'ADMIN' WHERE email = 'react@search.local'")
    token = login("spring@search.local")
    admin = login("react@search.local")
    stamp = str(int(time.time()))
    words = {k: f"{k}{stamp}" for k in ("keep", "oldtext", "newtext", "gone", "fresh")}

    def create(text):
        status, body, _ = request("/api/v1/feeds", "POST", {"content": text}, token)
        return body["data"]["id"]

    keep = create("복구 확인 " + words["keep"])
    target = create("복구 확인 " + words["oldtext"])
    gone = create("복구 확인 " + words["gone"])
    ready = wait_until(lambda: all(feed_total(words[k])[1] == 1 for k in ("keep", "oldtext", "gone")), 20)
    record("장애 전 색인 완료", "20초 안에 검색됨", f"{ready:.1f}초" if ready else "시간 초과", ready is not None)

    # 1. 실제 정지
    docker("stop", ES_CONTAINER)
    first = feed_total(words["keep"])
    second = feed_total(words["keep"])
    record("정지 직후 검색 응답", "200, PostgreSQL로 대체", f"{first[0]} / 건수 {first[1]} / {first[2] * 1000:.0f}ms", first[0] == 200 and first[1] == 1)
    record("회로 열린 뒤 검색 응답", "200", f"{second[0]} / {second[2] * 1000:.0f}ms", second[0] == 200)

    fresh = create("복구 확인 " + words["fresh"])
    status, _, _ = request(f"/api/v1/feeds/{target}", "PUT", {"content": "복구 확인 " + words["newtext"]}, token)
    deleted, _, _ = request(f"/api/v1/feeds/{gone}", "DELETE", None, token)
    record("장애 중 생성·수정·삭제 API", "성공", f"수정 {status}, 삭제 {deleted}", status == 200 and deleted == 204)
    record("장애 중 삭제 글 미노출", "0건", feed_total(words["gone"])[1], feed_total(words["gone"])[1] == 0)
    record("장애 중 새 글 검색", "1건(PostgreSQL)", feed_total(words["fresh"])[1], feed_total(words["fresh"])[1] == 1)
    record("장애 중 수정 전 단어 미노출", "0건", feed_total(words["oldtext"])[1], feed_total(words["oldtext"])[1] == 0)
    health = request("/actuator/health")[0]
    record("장애 중 서버 상태", "200", health, health == 200)

    time.sleep(40)
    pending = sql("SELECT count(*) FROM search_outbox WHERE processed_at IS NULL")
    attempts = sql("SELECT coalesce(max(attempts), 0) FROM search_outbox WHERE processed_at IS NULL")
    record("장애 중 대기열 보존", "3건 이상 대기", f"대기 {pending}건, 최대 시도 {attempts}", int(pending) >= 3)

    # 2. 복구
    docker("start", ES_CONTAINER)
    restored = time.time()
    wait_until(lambda: subprocess.run(["curl", "-sf", ES + "/_cluster/health"], capture_output=True).returncode == 0, 120)
    drained = wait_until(lambda: sql("SELECT count(*) FROM search_outbox WHERE processed_at IS NULL") == "0", 300)
    record("복구 후 대기열 소진", "300초 안에 0건", f"재시작 후 {time.time() - restored:.0f}초" if drained is not None else "시간 초과", drained is not None)

    wait_until(lambda: es_doc_exists(fresh), 30)
    record("장애 중 생성 문서 색인", "ES에 있음", es_doc_exists(fresh), es_doc_exists(fresh))
    record("장애 중 삭제 문서 제거", "ES에 없음", not es_doc_exists(gone), not es_doc_exists(gone))
    time.sleep(32)  # 회로가 닫힐 때까지 기다린다
    record("복구 후 수정 내용 검색(ES)", "새 단어 1건, 옛 단어 0건",
           f"새 {feed_total(words['newtext'])[1]}, 옛 {feed_total(words['oldtext'])[1]}",
           feed_total(words["newtext"])[1] == 1 and feed_total(words["oldtext"])[1] == 0)

    status, body, _ = request("/api/v1/admin/search/reconcile", "POST", None, admin)
    record("복구 후 정합성 점검", "모든 대상 차이 0건", body["data"] if body else status, body and all(v == 0 for v in body["data"].values()))

    # 3. 응답 없는 장애(프로세스는 살아 있음)
    docker("pause", ES_CONTAINER)
    hang = [feed_total(words["keep"]) for _ in range(3)]
    docker("unpause", ES_CONTAINER)
    record("응답 없는 장애", "첫 요청은 소켓 타임아웃(3초) 뒤 대체, 이후 즉시 대체",
           [f"{r[0]}/{r[2] * 1000:.0f}ms" for r in hang], all(r[0] == 200 for r in hang) and hang[1][2] < 2)

    passed = sum(1 for r in results if r["통과"])
    output = sys.argv[1] if len(sys.argv) > 1 else "outage-recovery-result.json"
    with open(output, "w", encoding="utf-8") as f:
        json.dump({"통과": passed, "전체": len(results), "결과": results}, f, ensure_ascii=False, indent=2)
    print(f"{passed}/{len(results)} 통과")


if __name__ == "__main__":
    main()
