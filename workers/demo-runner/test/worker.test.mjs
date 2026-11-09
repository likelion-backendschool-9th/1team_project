// wrangler의 로컬 런타임(workerd + 로컬 R2)으로 Worker를 실제로 띄워 검증한다. Cloudflare 계정은 쓰지 않는다.
import assert from "node:assert/strict";
import { after, before, describe, it } from "node:test";
import { unstable_dev } from "wrangler";

const ANCESTOR = "https://dipsource.test";
let worker;

const bucket = {
  put: async (key, body) => (await worker.fetch(`/__seed/${encodeURIComponent(key)}`, { method: "PUT", body })).text(),
  delete: async (key) => (await worker.fetch(`/__seed/${encodeURIComponent(key)}`, { method: "DELETE" })).text(),
  list: async (prefix) => (await worker.fetch(`/__seed/${encodeURIComponent(prefix)}`)).json(),
};

const publication = (versionId, demoId = 1) =>
  JSON.stringify({ versionId, storagePrefix: `demos/${demoId}/versions/${versionId}`, entryPath: "index.html" });

async function seed() {
  await bucket.put("demos/1/publication.json", publication(2));
  await bucket.put("demos/1/versions/2/files/index.html", "<!doctype html><html><head></head><body>v2</body></html>");
  await bucket.put("demos/1/versions/2/files/assets/app.js", "export const a = 1;");
  await bucket.put("demos/1/versions/2/files/assets/style.css", "body{}");
  await bucket.put("demos/1/versions/2/files/logo.svg", "<svg xmlns='http://www.w3.org/2000/svg'/>");
  await bucket.put("demos/1/versions/2/files/payload.exe", "MZ");
  await bucket.put("demos/1/versions/1/files/index.html", "old");
  // 공개 포인터가 없는 데모 (비공개·삭제)
  await bucket.put("demos/9/versions/3/files/index.html", "private");
  // 다른 데모 경로를 가리키는 조작된 포인터
  await bucket.put(
    "demos/5/publication.json",
    JSON.stringify({ versionId: 7, storagePrefix: "demos/9/versions/3", entryPath: "index.html" }),
  );
}

const call = (path, init) => worker.fetch(path, init);

before(async () => {
  worker = await unstable_dev("test/harness.js", {
    config: "wrangler.toml",
    vars: { ALLOWED_FRAME_ANCESTORS: ANCESTOR },
    logLevel: "none",
    experimental: { disableExperimentalWarning: true },
  });
  await seed();
});

after(async () => {
  await worker?.stop();
});

describe("정적 파일 제공", () => {
  it("공개 버전의 HTML을 CSP sandbox와 함께 내려 준다", async () => {
    const res = await call("/demos/1/versions/2/index.html");
    assert.equal(res.status, 200);
    assert.match(res.headers.get("content-type"), /^text\/html/);
    const csp = res.headers.get("content-security-policy");
    assert.match(csp, /sandbox allow-scripts(;|$)/);
    assert.doesNotMatch(csp, /allow-same-origin/);
    assert.match(csp, /default-src 'none'/);
    assert.match(csp, new RegExp(`frame-ancestors ${ANCESTOR}`));
    assert.match(csp, /connect-src http:\/\/[^ ]+\/demos\/1\/versions\/2\//);
    assert.equal(res.headers.get("x-content-type-options"), "nosniff");
    assert.match(await res.text(), /v2/);
  });

  it("버전 루트는 entryPath를 내려 준다", async () => {
    const res = await call("/demos/1/versions/2/");
    assert.equal(res.status, 200);
    assert.match(await res.text(), /v2/);
  });

  it("MIME 타입을 확장자로 정한다", async () => {
    const js = await call("/demos/1/versions/2/assets/app.js");
    assert.match(js.headers.get("content-type"), /^text\/javascript/);
    const css = await call("/demos/1/versions/2/assets/style.css");
    assert.match(css.headers.get("content-type"), /^text\/css/);
  });

  it("opaque origin의 module 로딩을 위해 CORS를 연다 (자격 증명 없이)", async () => {
    const res = await call("/demos/1/versions/2/assets/app.js", { headers: { Origin: "null" } });
    assert.equal(res.status, 200);
    assert.equal(res.headers.get("access-control-allow-origin"), "*");
    assert.equal(res.headers.get("access-control-allow-credentials"), null);
  });

  it("SVG 같은 HTML 외 응답에도 sandbox CSP를 붙인다", async () => {
    const res = await call("/demos/1/versions/2/logo.svg");
    assert.match(res.headers.get("content-security-policy"), /sandbox allow-scripts/);
  });

  it("ETag가 같으면 304를 준다", async () => {
    const first = await call("/demos/1/versions/2/assets/app.js");
    await first.arrayBuffer();
    const res = await call("/demos/1/versions/2/assets/app.js", {
      headers: { "If-None-Match": first.headers.get("etag") },
    });
    assert.equal(res.status, 304);
  });

  it("HEAD는 본문 없이 헤더만 준다", async () => {
    const res = await call("/demos/1/versions/2/index.html", { method: "HEAD" });
    assert.equal(res.status, 200);
    assert.equal(await res.text(), "");
  });
});

describe("접근 차단", () => {
  for (const method of ["POST", "PUT", "DELETE", "PATCH"]) {
    it(`${method}는 405`, async () => {
      const res = await call("/demos/1/versions/2/index.html", { method });
      assert.equal(res.status, 405);
      assert.equal(res.headers.get("allow"), "GET, HEAD");
    });
  }

  it("이전 버전은 파일이 있어도 404", async () => {
    assert.equal((await call("/demos/1/versions/1/index.html")).status, 404);
  });

  it("공개 포인터가 없는 데모는 404", async () => {
    assert.equal((await call("/demos/9/versions/3/index.html")).status, 404);
  });

  it("다른 데모를 가리키는 포인터는 무시한다", async () => {
    assert.equal((await call("/demos/5/versions/7/index.html")).status, 404);
  });

  it("공개 포인터를 지우면 바로 막힌다", async () => {
    await bucket.put("demos/2/publication.json", publication(4, 2));
    await bucket.put("demos/2/versions/4/files/index.html", "soon gone");
    assert.equal((await call("/demos/2/versions/4/index.html")).status, 200);
    await bucket.delete("demos/2/publication.json");
    assert.equal((await call("/demos/2/versions/4/index.html")).status, 404);
  });

  it("경로 조작을 막는다", async () => {
    for (const path of [
      "/demos/1/versions/2/%2e%2e/1/index.html",
      "/demos/1/versions/2/..%2f..%2fpublication.json",
      "/demos/1/versions/2/a%5cb.js",
      "/demos/1/versions/2/%00.js",
      "/demos/1/versions/2/.hidden.js",
      "/demos/1/versions/2/assets//app.js",
      "/demos/1/versions/2/assets/",
      "/demos/1/publication.json",
      "/demos/abc/versions/2/index.html",
    ]) {
      assert.equal((await call(path)).status, 404, path);
    }
  });

  it("허용하지 않은 확장자는 404", async () => {
    assert.equal((await call("/demos/1/versions/2/payload.exe")).status, 404);
  });

  it("없는 파일과 없는 라우트는 404", async () => {
    assert.equal((await call("/demos/1/versions/2/missing.js")).status, 404);
    assert.equal((await call("/")).status, 404);
  });
});
