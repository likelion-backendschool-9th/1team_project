#!/usr/bin/env python3
"""백엔드 테스트를 실행 환경별로 분류한다. ./gradlew test 뒤에 실행한다. 사용: classify-tests.py [--since <git-ref>]"""
import collections
import glob
import pathlib
import re
import subprocess
import sys
import xml.etree.ElementTree as ET

ROOT = pathlib.Path(__file__).resolve().parents[2]
SRC = ROOT / "src/test/java"


def source_of(classname):
    top = classname.split("$")[0]
    path = SRC / (top.replace(".", "/") + ".java")
    return path, path.read_text(encoding="utf-8") if path.exists() else ""


def category(text):
    uses_es = "SearchContainers" in text or "AbstractSearchVerification" in text
    unreachable = "localhost:1" in text
    if unreachable:
        return "통합: 실제 PostgreSQL + ES 접속 불가(장애 시뮬레이션)"
    if uses_es:
        return "통합: 실제 PostgreSQL + 실제 Elasticsearch"
    if "PostgreSQLContainer" in text or "@Testcontainers" in text:
        return "통합: 실제 PostgreSQL"
    if "@SpringBootTest" in text:
        return "통합: Spring 컨텍스트"
    if "@WebMvcTest" in text or "standaloneSetup" in text:
        return "슬라이스: MVC(모의 서비스)"
    if "@DataJpaTest" in text:
        return "슬라이스: JPA"
    return "단위"


def changed_since(ref):
    if not ref:
        return set()
    out = subprocess.run(["git", "diff", "--name-only", "--diff-filter=A", ref, "HEAD", "--", "src/test/java"],
                         cwd=ROOT, capture_output=True, text=True, check=True).stdout
    return {pathlib.Path(line).stem for line in out.splitlines()}


def main():
    ref = sys.argv[sys.argv.index("--since") + 1] if "--since" in sys.argv else None
    added = changed_since(ref)
    rows = collections.defaultdict(lambda: [0, 0])
    per_class = []
    for file in glob.glob(str(ROOT / "build/test-results/test/*.xml")):
        root = ET.parse(file).getroot()
        # 중첩 클래스는 name이 표시 이름이라 파일 이름에서 실제 클래스 이름을 얻는다
        classname = pathlib.Path(file).name.removeprefix("TEST-").removesuffix(".xml")
        _, text = source_of(classname)
        cat = category(text)
        top = classname.split("$")[0].split(".")[-1]
        tests = int(root.get("tests"))
        fails = int(root.get("failures")) + int(root.get("errors"))
        is_new = top in added
        rows[(cat, "신규" if is_new else "기존")][0] += tests
        rows[(cat, "신규" if is_new else "기존")][1] += fails
        per_class.append((cat, "신규" if is_new else "기존", classname.replace("com.dipsource.", ""), tests, fails))

    print("| 분류 | 구분 | 테스트 수 | 실패 |\n|---|---|---|---|")
    total = collections.Counter()
    for (cat, kind), (tests, fails) in sorted(rows.items()):
        print(f"| {cat} | {kind} | {tests} | {fails} |")
        total[kind] += tests
    print(f"\n기존 {total['기존']}건, 신규 {total['신규']}건, 합계 {sum(total.values())}건\n")
    print("| 분류 | 구분 | 클래스 | 테스트 수 | 실패 |\n|---|---|---|---|---|")
    for row in sorted(per_class):
        print("| " + " | ".join(str(c) for c in row) + " |")


if __name__ == "__main__":
    main()
