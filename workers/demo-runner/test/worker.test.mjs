// wrangler의 로컬 런타임(workerd + 로컬 R2)으로 Worker를 실제로 띄워 검증한다. Cloudflare 계정은 쓰지 않는다.
import assert from "node:assert/strict";
import { after, before, describe, it } from "node:test";
import { unstable_dev } from "wrangler";

import { createHmac, randomUUID } from "node:crypto";

const ANCESTOR = "https://dipsource.test";
const SECRET = "test-signing-secret";
const NONCE = "abcdefghijklmnop1234";

function signedPath(prefix, expiresInSeconds, file = "index.html", secret = SECRET, mockKey = undefined) {
  const scope = Buffer.from(JSON.stringify(mockKey ? { p: prefix, m: mockKey } : { p: prefix })).toString("base64url");
  const expires = Math.floor(Date.now() / 1000) + expiresInSeconds;
  const signature = createHmac("sha256", secret).update(`${scope}.${expires}`).digest("base64url");
  return `/p/${scope}/${expires}.${signature}/${file}`;
}
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
    vars: { ALLOWED_FRAME_ANCESTORS: ANCESTOR, DEMO_SIGNING_SECRET: SECRET },
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

describe("서명 미리보기", () => {
  const previewId = randomUUID();
  const prefix = `previews/${previewId}`;

  before(async () => {
    await bucket.put(`${prefix}/files/index.html`, "<!doctype html><html><head></head><body>preview</body></html>");
    await bucket.put(`${prefix}/files/assets/app.js`, "1");
  });

  it("유효한 서명이면 공개 포인터 없이 읽는다", async () => {
    const res = await call(signedPath(prefix, 60));
    assert.equal(res.status, 200);
    assert.match(await res.text(), /preview/);
    assert.match(res.headers.get("content-security-policy"), /sandbox allow-scripts/);
  });

  it("상대 경로 자산도 같은 토큰 아래에서 읽힌다", async () => {
    assert.equal((await call(signedPath(prefix, 60, "assets/app.js"))).status, 200);
  });

  it("서명이 없거나 공개 경로로 접근하면 차단한다", async () => {
    assert.equal((await call(`/${prefix}/files/index.html`)).status, 404);
    assert.equal((await call(`/previews/${previewId}/index.html`)).status, 404);
  });

  it("만료되면 403", async () => {
    const res = await call(signedPath(prefix, -1));
    assert.equal(res.status, 403);
    assert.equal((await res.json()).error, "expired");
  });

  it("다른 키로 서명하면 403", async () => {
    assert.equal((await call(signedPath(prefix, 60, "index.html", "wrong"))).status, 403);
  });

  it("다른 경로로 scope를 바꿔치기하면 403", async () => {
    const good = signedPath(prefix, 60).split("/");
    good[2] = Buffer.from(JSON.stringify({ p: "demos/9/versions/3" })).toString("base64url");
    assert.equal((await call(good.join("/"))).status, 403);
  });

  it("서명이 맞아도 허용 범위 밖 경로는 읽지 않는다", async () => {
    assert.equal((await call(signedPath("demos/1/publication.json", 60))).status, 403);
    assert.equal((await call(signedPath("../secret", 60))).status, 403);
  });

  it("서명 경로에서도 경로 조작을 막는다", async () => {
    assert.equal((await call(signedPath(prefix, 60, "..%2f..%2fx.js"))).status, 404);
  });

  it("POST는 405", async () => {
    assert.equal((await call(signedPath(prefix, 60), { method: "POST" })).status, 405);
  });

  it("버전 경로도 서명으로 미리 볼 수 있다", async () => {
    const res = await call(signedPath("demos/1/versions/1", 60));
    assert.equal(res.status, 200);
    assert.equal(await res.text(), "old");
  });
});

describe("썸네일", () => {
  const name = `${randomUUID()}.jpg`;

  it("저장된 썸네일을 이미지로 준다", async () => {
    await bucket.put(`demos/1/thumbnails/${name}`, "jpeg");
    const res = await call(`/thumbnails/1/${name}`);
    assert.equal(res.status, 200);
    assert.equal(res.headers.get("content-type"), "image/jpeg");
  });

  it("없거나 형식이 다르면 404", async () => {
    assert.equal((await call(`/thumbnails/1/${randomUUID()}.jpg`)).status, 404);
    assert.equal((await call("/thumbnails/1/publication.json")).status, 404);
  });
});

describe("실행 상태 스크립트", () => {
  it("nonce가 있으면 head 맨 앞에 넣는다", async () => {
    const html = await (await call(`/demos/1/versions/2/index.html?dip_nonce=${NONCE}`)).text();
    assert.match(html, /<head><script>\(function\(\)\{var N="abcdefghijklmnop1234"/);
    assert.ok(html.indexOf("dipsource-demo") < html.indexOf("v2"));
  });

  it("nonce가 없으면 파일을 그대로 내려 준다", async () => {
    assert.doesNotMatch(await (await call("/demos/1/versions/2/index.html")).text(), /dipsource-demo/);
  });

  it("nonce 형식이 다르면 넣지 않아 스크립트 주입을 막는다", async () => {
    const res = await call(`/demos/1/versions/2/index.html?dip_nonce=${encodeURIComponent('"></script><script>x')}`);
    assert.doesNotMatch(await res.text(), /dipsource-demo/);
  });

  it("head 없는 문서에도 넣는다", async () => {
    await bucket.put("demos/1/versions/2/files/nohead.html", "<p>hi</p>");
    const html = await (await call(`/demos/1/versions/2/nohead.html?dip_nonce=${NONCE}`)).text();
    assert.ok(html.indexOf("dipsource-demo") < html.indexOf("<p>hi"));
  });

  it("JS에는 넣지 않는다", async () => {
    assert.equal(await (await call(`/demos/1/versions/2/assets/app.js?dip_nonce=${NONCE}`)).text(), "export const a = 1;");
  });

  it("iframe 탐색이 실패하면 부모에 오류를 알리는 HTML을 준다", async () => {
    const res = await call(`/demos/1/versions/2/missing.html?dip_nonce=${NONCE}`, {
      headers: { "Sec-Fetch-Dest": "iframe" },
    });
    assert.equal(res.status, 404);
    assert.match(res.headers.get("content-type"), /^text\/html/);
    const html = await res.text();
    assert.match(html, /type:"error"/);
    assert.match(html, /status:404/);
  });

  it("nonce 없는 실패와 하위 자원 실패는 JSON이다", async () => {
    const plain = await call("/demos/1/versions/2/missing.html", { headers: { "Sec-Fetch-Dest": "iframe" } });
    assert.match(plain.headers.get("content-type"), /json/);
    const asset = await call(`/demos/1/versions/2/missing.js?dip_nonce=${NONCE}`, { headers: { "Sec-Fetch-Dest": "script" } });
    assert.match(asset.headers.get("content-type"), /json/);
  });
});

describe("Mock API", () => {
  const demo = 7;
  const version = 70;
  const mockKey = `demos/${demo}/versions/${version}/mock.json`;
  const base = `/demos/${demo}/versions/${version}/__mock`;
  const endpoints = [
    { method: "GET", path: "/products", status: 200, response: [{ id: 1, name: "Keyboard" }] },
    { method: "POST", path: "/orders", status: 201, response: { id: 10 } },
    { method: "PUT", path: "/orders", status: 200, response: { updated: true } },
    { method: "DELETE", path: "/orders/1", status: 204 },
    { method: "GET", path: "/broken", status: 500, response: { error: "fail" } },
  ];

  before(async () => {
    await bucket.put(`demos/${demo}/publication.json`, JSON.stringify({
      versionId: version, storagePrefix: `demos/${demo}/versions/${version}`, entryPath: "index.html", mockKey,
    }));
    await bucket.put(mockKey, JSON.stringify({ endpoints }));
    await bucket.put(`demos/${demo}/versions/${version}/files/index.html`, "<p>");
    await bucket.put(`demos/${demo}/versions/69/mock.json`, JSON.stringify({ endpoints }));
  });

  it("GET 응답", async () => {
    const res = await call(`${base}/products`);
    assert.equal(res.status, 200);
    assert.match(res.headers.get("content-type"), /json/);
    assert.deepEqual(await res.json(), [{ id: 1, name: "Keyboard" }]);
  });

  it("POST 응답과 등록한 상태 코드", async () => {
    const res = await call(`${base}/orders`, { method: "POST", body: "{}", headers: { "Content-Type": "application/json" } });
    assert.equal(res.status, 201);
    assert.deepEqual(await res.json(), { id: 10 });
  });

  it("204는 본문이 없다", async () => {
    const res = await call(`${base}/orders/1`, { method: "DELETE" });
    assert.equal(res.status, 204);
    assert.equal(await res.text(), "");
  });

  it("등록한 오류 상태를 그대로 준다", async () => {
    assert.equal((await call(`${base}/broken`)).status, 500);
  });

  it("등록하지 않은 경로는 404, 메서드만 다르면 405", async () => {
    assert.equal((await call(`${base}/users`)).status, 404);
    const res = await call(`${base}/products`, { method: "POST" });
    assert.equal(res.status, 405);
    assert.equal(res.headers.get("allow"), "GET");
  });

  it("쓰기 요청이 R2를 바꾸지 않는다", async () => {
    const before = await bucket.list(`demos/${demo}/`);
    await call(`${base}/orders`, { method: "POST", body: JSON.stringify({ evil: 1 }) });
    await call(`${base}/orders`, { method: "PUT", body: "x" });
    await call(`${base}/orders/1`, { method: "DELETE" });
    assert.deepEqual(await bucket.list(`demos/${demo}/`), before);
    assert.deepEqual(await (await call(`${base}/products`)).json(), [{ id: 1, name: "Keyboard" }]);
  });

  it("opaque origin의 preflight를 허용한다 (등록한 메서드만)", async () => {
    const res = await call(`${base}/orders`, {
      method: "OPTIONS",
      headers: { Origin: "null", "Access-Control-Request-Method": "POST", "Access-Control-Request-Headers": "content-type" },
    });
    assert.equal(res.status, 204);
    assert.equal(res.headers.get("access-control-allow-origin"), "*");
    assert.equal(res.headers.get("access-control-allow-methods"), "POST, PUT");
    assert.equal(res.headers.get("access-control-allow-credentials"), null);

    const denied = await call(`${base}/orders`, { method: "OPTIONS", headers: { "Access-Control-Request-Method": "PATCH" } });
    assert.equal(denied.status, 405);
    const unknown = await call(`${base}/nope`, { method: "OPTIONS", headers: { "Access-Control-Request-Method": "GET" } });
    assert.equal(unknown.status, 404);
  });

  it("응답에 CORS를 열되 쿠키를 설정하지 않는다", async () => {
    const res = await call(`${base}/products`, { headers: { Origin: "null", Cookie: "secret=1" } });
    assert.equal(res.headers.get("access-control-allow-origin"), "*");
    assert.equal(res.headers.get("set-cookie"), null);
    assert.doesNotMatch(await res.text(), /secret/);
  });

  it("공개되지 않은 버전의 Mock은 404", async () => {
    assert.equal((await call(`/demos/${demo}/versions/69/__mock/products`)).status, 404);
    assert.equal((await call("/demos/9/versions/3/__mock/products")).status, 404);
  });

  it("Mock이 없는 공개 버전은 404", async () => {
    assert.equal((await call("/demos/1/versions/2/__mock/products")).status, 404);
  });

  it("정적 경로의 쓰기 금지는 그대로다", async () => {
    assert.equal((await call(`/demos/${demo}/versions/${version}/index.html`, { method: "POST" })).status, 405);
    assert.equal((await call(`/demos/${demo}/versions/${version}/index.html`, { method: "OPTIONS" })).status, 405);
  });

  it("__mock 아래로 정적 파일을 읽을 수 없다", async () => {
    assert.equal((await call(`${base}/../mock.json`)).status, 404);
    assert.equal((await call(`/demos/${demo}/versions/${version}/mock.json`)).status, 404);
  });

  it("서명 경로의 Mock은 grant의 키만 읽는다", async () => {
    const file = signedPath(`demos/${demo}/versions/${version}`, 60, "__mock/products", SECRET, mockKey);
    assert.deepEqual(await (await call(file)).json(), [{ id: 1, name: "Keyboard" }]);
    const noMock = signedPath(`demos/${demo}/versions/${version}`, 60, "__mock/products");
    assert.equal((await call(noMock)).status, 404);
    const expired = signedPath(`demos/${demo}/versions/${version}`, -1, "__mock/products", SECRET, mockKey);
    assert.equal((await call(expired)).status, 403);
    const badKey = signedPath(`demos/${demo}/versions/${version}`, 60, "__mock/products", SECRET, "demos/1/publication.json");
    assert.equal((await call(badKey)).status, 403);
  });

  it("CSP가 Mock 경로로의 fetch를 허용하는 범위를 넓히지 않는다", async () => {
    const csp = (await call(`/demos/${demo}/versions/${version}/index.html`)).headers.get("content-security-policy");
    assert.match(csp, new RegExp(`connect-src http://[^ ]+/demos/${demo}/versions/${version}/(;|$)`));
  });
});
