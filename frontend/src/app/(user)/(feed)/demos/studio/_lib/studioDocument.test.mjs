import assert from "node:assert/strict";
import { describe, it } from "node:test";
import { checkJson, composeStudioDocument, MAX_SAMPLE_BYTES, resolveMockRows } from "./studioDocument.ts";

const code = (overrides = {}) => ({ html: "<p>hi</p>", css: "", js: "", mockConfig: "", sampleData: "", ...overrides });

describe("checkJson", () => {
  it("빈 값과 올바른 JSON을 통과시킨다", () => {
    assert.equal(checkJson("  ", 100).ok, true);
    assert.deepEqual(checkJson('{"a":1}', 100), { ok: true, value: { a: 1 } });
  });

  it("문법 오류를 알린다", () => {
    const result = checkJson("{broken", 100);
    assert.equal(result.ok, false);
    assert.match(result.message, /JSON 문법 오류/);
  });

  it("크기를 제한한다 (멀티바이트 기준)", () => {
    assert.equal(checkJson(`"${"가".repeat(MAX_SAMPLE_BYTES / 3 + 1)}"`, MAX_SAMPLE_BYTES).ok, false);
  });

  it("중첩 깊이를 제한한다", () => {
    assert.equal(checkJson("[".repeat(17) + "]".repeat(17), 1000).ok, false);
    assert.equal(checkJson("[".repeat(16) + "]".repeat(16), 1000).ok, true);
  });
});

describe("resolveMockRows", () => {
  const config = JSON.stringify({
    endpoints: [
      { method: "GET", path: "/p", dataKey: "products" },
      { method: "POST", path: "/o", status: 201, response: { id: 1 } },
      "잘못된 항목",
    ],
  });

  it("dataKey를 샘플 데이터로 푼다", () => {
    const rows = resolveMockRows(config, '{"products":[1,2]}');
    assert.deepEqual(rows[0], { method: "GET", path: "/p", status: 200, response: [1, 2] });
    assert.equal(rows[1].status, 201);
    assert.equal(rows.length, 2);
  });

  it("설정이 깨져 있으면 빈 표", () => {
    assert.deepEqual(resolveMockRows("{broken", ""), []);
    assert.deepEqual(resolveMockRows('{"endpoints":1}', ""), []);
  });
});

describe("composeStudioDocument", () => {
  it("HTML 조각을 문서로 감싸고 코드를 합친다", () => {
    const doc = composeStudioDocument(code({ css: "b{}", js: "run()" }));
    assert.match(doc, /^<!doctype html>/);
    assert.match(doc, /<style>b\{\}<\/style>/);
    assert.match(doc, /<script>run\(\)<\/script>/);
    assert.ok(doc.indexOf("<p>hi</p>") < doc.indexOf("<script>run()"));
  });

  it("사용자 스크립트보다 CSP와 Mock 대체가 먼저 온다", () => {
    const doc = composeStudioDocument(code({ js: "userCode()" }));
    assert.ok(doc.indexOf("Content-Security-Policy") < doc.indexOf("window.fetch"));
    assert.ok(doc.indexOf("window.fetch") < doc.indexOf("userCode()"));
    assert.match(doc, /connect-src 'none'/);
  });

  it("완성된 문서에는 head 안에 넣는다", () => {
    const doc = composeStudioDocument(code({ html: "<!doctype html><html><head><title>t</title></head><body><p>x</p></body></html>", js: "go()" }));
    assert.equal(doc.match(/<html/g).length, 1);
    assert.ok(doc.indexOf("Content-Security-Policy") < doc.indexOf("<title>"));
    assert.ok(doc.indexOf("go()") < doc.indexOf("</body>"));
  });

  it("코드가 스크립트·스타일 태그를 닫아 빠져나오지 못한다", () => {
    const doc = composeStudioDocument(code({ js: 'x="</script><img src=x>"', css: "</style><script>y</script>" }));
    assert.doesNotMatch(doc, /<\/script><img/);
    assert.doesNotMatch(doc, /<\/style><script>y/);
  });

  it("Mock 응답에 든 태그가 문서를 깨지 않는다", () => {
    const mockConfig = JSON.stringify({ endpoints: [{ method: "GET", path: "/a", response: "</script><b>" }] });
    assert.doesNotMatch(composeStudioDocument(code({ mockConfig })), /"<\/script><b>"/);
  });
});
