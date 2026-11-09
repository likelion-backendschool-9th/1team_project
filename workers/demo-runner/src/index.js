// 찍먹 데모 실행 Worker.
// 비공개 R2의 정적 파일을 내려 주기만 한다. 사용자 코드는 서버에서 실행하지 않고 브라우저의 sandbox 안에서만 돈다.

const MOCK_ROUTE = /^\/demos\/(\d{1,18})\/versions\/(\d{1,18})\/__mock(\/.*)$/;
const SIGNED_MOCK_PATH = /^__mock(\/.*)$/;
const MOCK_KEY = /^demos\/\d{1,18}\/versions\/\d{1,18}\/mock\.json$/;
const DEMO_ROUTE = /^\/demos\/(\d{1,18})\/versions\/(\d{1,18})\/(.*)$/;
const SIGNED_ROUTE = /^\/p\/([A-Za-z0-9_-]{1,512})\/(\d{1,12})\.([A-Za-z0-9_-]{43})\/(.*)$/;
const THUMBNAIL_ROUTE = /^\/thumbnails\/(\d{1,18})\/([0-9a-f-]{36}\.jpg)$/;
const SIGNED_PREFIX = /^(previews\/[0-9a-f-]{36}|demos\/\d{1,18}\/versions\/\d{1,18})$/;
const RESERVED_ROOT = "__mock";
const NONCE = /^[A-Za-z0-9_-]{16,64}$/;

const MIME_TYPES = {
  html: "text/html; charset=utf-8",
  htm: "text/html; charset=utf-8",
  css: "text/css; charset=utf-8",
  js: "text/javascript; charset=utf-8",
  mjs: "text/javascript; charset=utf-8",
  json: "application/json; charset=utf-8",
  map: "application/json; charset=utf-8",
  txt: "text/plain; charset=utf-8",
  png: "image/png",
  jpg: "image/jpeg",
  jpeg: "image/jpeg",
  gif: "image/gif",
  svg: "image/svg+xml",
  webp: "image/webp",
  avif: "image/avif",
  ico: "image/x-icon",
  woff: "font/woff",
  woff2: "font/woff2",
  ttf: "font/ttf",
  otf: "font/otf",
};

export default {
  async fetch(request, env) {
    try {
      return await route(request, env);
    } catch (error) {
      console.error("demo-runner failure", error);
      return failure(request, env, readNonce(new URL(request.url)), 500, "internal_error");
    }
  },
};

async function route(request, env) {
  const url = new URL(request.url);
  const nonce = readNonce(url);

  // Mock 경로만 쓰기 메서드를 받는다. 정적 파일 경로의 GET/HEAD 제한은 그대로다
  const mock = MOCK_ROUTE.exec(url.pathname);
  if (mock) return serveMockFromPublication(request, env, mock[1], mock[2], mock[3]);
  const signedMock = signedMockRequest(url);
  if (signedMock) return serveSignedMock(request, env, signedMock);

  if (request.method !== "GET" && request.method !== "HEAD") {
    return errorResponse(405, "method_not_allowed", { Allow: "GET, HEAD" });
  }

  const thumbnail = THUMBNAIL_ROUTE.exec(url.pathname);
  if (thumbnail) return serveThumbnail(request, env, thumbnail[1], thumbnail[2]);

  const signed = SIGNED_ROUTE.exec(url.pathname);
  if (signed) return serveSigned(request, env, url, signed, nonce);

  const match = DEMO_ROUTE.exec(url.pathname);
  if (!match) return failure(request, env, nonce, 404, "not_found");
  const [, demoId, versionId, rawPath] = match;

  const publication = await readPublication(env, demoId);
  // 공개 포인터가 가리키는 버전만 내려 준다. 비공개·삭제된 데모와 이전 버전은 URL을 알아도 404다
  if (!publication || String(publication.versionId) !== versionId) {
    return failure(request, env, nonce, 404, "not_found");
  }
  const filePath = rawPath === "" ? publication.entryPath : decodeFilePath(rawPath);
  if (!filePath) return failure(request, env, nonce, 404, "not_found");

  return serveFile(request, env, {
    objectKey: `${publication.storagePrefix}/files/${filePath}`,
    filePath,
    scope: `${url.origin}/demos/${demoId}/versions/${versionId}/`,
    nonce,
  });
}

/** 공개 전 미리보기·썸네일 촬영용 경로. 서명과 만료 시각이 맞을 때만 비공개 경로를 읽는다. */
async function serveSigned(request, env, url, [, scope, expires, signature, rawPath], nonce) {
  if (!(await verifySignature(env, `${scope}.${expires}`, signature))) {
    return failure(request, env, nonce, 403, "invalid_signature");
  }
  if (Number(expires) * 1000 <= Date.now()) return failure(request, env, nonce, 403, "expired");

  const grant = parseGrant(scope);
  if (!grant) return failure(request, env, nonce, 403, "invalid_signature");
  const filePath = rawPath === "" ? "index.html" : decodeFilePath(rawPath);
  if (!filePath) return failure(request, env, nonce, 404, "not_found");

  return serveFile(request, env, {
    objectKey: `${grant.p}/files/${filePath}`,
    filePath,
    scope: `${url.origin}/p/${scope}/${expires}.${signature}/`,
    nonce,
  });
}

function parseGrant(scope) {
  try {
    const grant = JSON.parse(atob(scope.replaceAll("-", "+").replaceAll("_", "/")));
    if (!SIGNED_PREFIX.test(grant.p) || (grant.m !== undefined && !MOCK_KEY.test(grant.m))) return null;
    return grant;
  } catch {
    return null;
  }
}

async function verifySignature(env, data, signature) {
  if (!env.DEMO_SIGNING_SECRET) return false;
  const encoder = new TextEncoder();
  const key = await crypto.subtle.importKey(
    "raw",
    encoder.encode(env.DEMO_SIGNING_SECRET),
    { name: "HMAC", hash: "SHA-256" },
    false,
    ["verify"],
  );
  const bytes = Uint8Array.from(atob(signature.replaceAll("-", "+").replaceAll("_", "/")), (c) => c.charCodeAt(0));
  return crypto.subtle.verify("HMAC", key, bytes, encoder.encode(data));
}

async function serveThumbnail(request, env, demoId, name) {
  const key = `demos/${demoId}/thumbnails/${name}`;
  const object = request.method === "HEAD" ? await env.DEMO_BUCKET.head(key) : await env.DEMO_BUCKET.get(key);
  if (!object) return errorResponse(404, "not_found");
  const headers = new Headers({
    "Content-Type": "image/jpeg",
    "Content-Security-Policy": "sandbox; default-src 'none'",
    "X-Content-Type-Options": "nosniff",
    "Access-Control-Allow-Origin": "*",
    "Cross-Origin-Resource-Policy": "cross-origin",
    // 파일명이 UUID라 내용이 바뀌지 않는다
    "Cache-Control": "public, max-age=86400",
  });
  return new Response(request.method === "HEAD" ? null : object.body, { headers });
}

function signedMockRequest(url) {
  const signed = SIGNED_ROUTE.exec(url.pathname);
  if (!signed) return null;
  const path = SIGNED_MOCK_PATH.exec(signed[4]);
  return path ? { scope: signed[1], expires: signed[2], signature: signed[3], path: path[1] } : null;
}

async function serveSignedMock(request, env, { scope, expires, signature, path }) {
  const valid = (await verifySignature(env, `${scope}.${expires}`, signature)) && Number(expires) * 1000 > Date.now();
  const grant = valid ? parseGrant(scope) : null;
  if (!grant) return mockError(403, "invalid_signature");
  if (!grant.m) return mockError(404, "not_found");
  return serveMock(request, env, grant.m, path);
}

async function serveMockFromPublication(request, env, demoId, versionId, path) {
  const publication = await readPublication(env, demoId);
  if (!publication || String(publication.versionId) !== versionId || !publication.mockKey) {
    return mockError(404, "not_found");
  }
  return serveMock(request, env, publication.mockKey, path);
}

/**
 * 등록된 정적 응답만 돌려준다. 요청 본문은 읽지 않고, R2·DB에 아무것도 쓰지 않으며, 사용자 코드를 실행하지 않는다.
 * 호출자는 sandbox의 opaque origin이라 쿠키 없이 CORS만 연다.
 */
async function serveMock(request, env, mockKey, path) {
  if (!MOCK_KEY.test(mockKey)) return mockError(404, "not_found");
  const object = await env.DEMO_BUCKET.get(mockKey);
  if (!object) return mockError(404, "not_found");
  const { endpoints } = await object.json();

  const samePath = endpoints.filter((endpoint) => endpoint.path === path);
  if (samePath.length === 0) return mockError(404, "not_found");
  const methods = samePath.map((endpoint) => endpoint.method);

  if (request.method === "OPTIONS") {
    if (!methods.includes(request.headers.get("Access-Control-Request-Method"))) {
      return mockError(405, "method_not_allowed");
    }
    return new Response(null, {
      status: 204,
      headers: {
        "Access-Control-Allow-Origin": "*",
        "Access-Control-Allow-Methods": methods.join(", "),
        "Access-Control-Allow-Headers": "content-type, accept",
        "Access-Control-Max-Age": "600",
      },
    });
  }

  const endpoint = samePath.find((item) => item.method === request.method);
  if (!endpoint) return mockError(405, "method_not_allowed", { Allow: methods.join(", ") });

  const body = endpoint.status === 204 ? null : JSON.stringify(endpoint.response ?? null);
  return new Response(body, { status: endpoint.status, headers: mockHeaders() });
}

function mockHeaders(extra = {}) {
  return {
    "Content-Type": "application/json; charset=utf-8",
    "X-Content-Type-Options": "nosniff",
    "Content-Security-Policy": "sandbox; default-src 'none'",
    "Access-Control-Allow-Origin": "*",
    "Cache-Control": "no-store",
    ...extra,
  };
}

function mockError(status, code, extra = {}) {
  return new Response(JSON.stringify({ error: code }), { status, headers: mockHeaders(extra) });
}

async function readPublication(env, demoId) {
  const object = await env.DEMO_BUCKET.get(`demos/${demoId}/publication.json`);
  if (!object) return null;
  const publication = await object.json();
  // 포인터는 자기 데모 아래만 가리킬 수 있다
  if (!String(publication.storagePrefix ?? "").startsWith(`demos/${demoId}/versions/`)) return null;
  return publication;
}

/** URL 경로를 R2 키로 쓸 수 있는 형태로 푼다. 조금이라도 이상하면 null. */
function decodeFilePath(rawPath) {
  const segments = [];
  for (const raw of rawPath.split("/")) {
    let segment;
    try {
      segment = decodeURIComponent(raw);
    } catch {
      return null;
    }
    if (!segment || segment.startsWith(".") || /[\\/\u0000-\u001f\u007f]/.test(segment)) return null;
    segments.push(segment);
  }
  return segments[0] === RESERVED_ROOT ? null : segments.join("/");
}

async function serveFile(request, env, { objectKey, filePath, scope, nonce }) {
  const contentType = MIME_TYPES[filePath.split(".").pop().toLowerCase()];
  if (!contentType) return failure(request, env, nonce, 404, "not_found");

  const headers = staticHeaders(env, scope);
  headers.set("Content-Type", contentType);

  if (request.method === "HEAD") {
    const head = await env.DEMO_BUCKET.head(objectKey);
    if (!head) return failure(request, env, nonce, 404, "not_found");
    headers.set("ETag", head.httpEtag);
    headers.set("Content-Length", String(head.size));
    return new Response(null, { headers });
  }

  const object = await env.DEMO_BUCKET.get(objectKey, { onlyIf: request.headers });
  if (!object) return failure(request, env, nonce, 404, "not_found");
  headers.set("ETag", object.httpEtag);
  // onlyIf 조건에 걸리면 본문 없는 객체가 온다 (If-None-Match 일치)
  if (!("body" in object)) return new Response(null, { status: 304, headers });
  const response = new Response(object.body, { headers });
  return nonce && contentType.startsWith("text/html") ? injectStatusScript(response, nonce) : response;
}

function readNonce(url) {
  const nonce = url.searchParams.get("dip_nonce");
  return nonce && NONCE.test(nonce) ? nonce : null;
}

/** 부모 창에 실행 상태를 알리는 스크립트. 사용자 스크립트보다 먼저 돌도록 가장 앞에 넣는다. */
function statusScript(nonce) {
  return `<script>(function(){var N="${nonce}",S="dipsource-demo",M=200;
function c(v){return String(v==null?"":v).slice(0,M)}
function p(t,d){try{parent.postMessage({source:S,nonce:N,type:t,detail:d||null},"*")}catch(e){}}
p("boot");
addEventListener("error",function(e){var t=e.target;if(t&&t!==window&&t.tagName){p("error",{kind:"resource",tag:c(t.tagName),message:c(t.src||t.href)})}else{p("error",{kind:"script",message:c(e.message)})}},true);
addEventListener("unhandledrejection",function(e){var r=e.reason;p("error",{kind:"promise",message:c(r&&r.message||r)})});
addEventListener("securitypolicyviolation",function(e){p("error",{kind:"csp",message:c(e.violatedDirective+" "+e.blockedURI)})});
addEventListener("load",function(){setTimeout(function(){p("loaded")},0)});
window.dipDemo={ready:function(){p("ready")},error:function(m){p("error",{kind:"app",message:c(m)})}};
})();</script>`;
}

function injectStatusScript(response, nonce) {
  let injected = false;
  const script = statusScript(nonce);
  return new HTMLRewriter()
    .on("*", {
      element(element) {
        if (injected || element.tagName === "html") return;
        injected = true;
        if (element.tagName === "head") element.prepend(script, { html: true });
        else element.before(script, { html: true });
      },
    })
    .transform(response);
}

/** 실행 화면(iframe)이 받을 오류는 부모가 알 수 있게 HTML로 내려 주고, 나머지 요청은 JSON으로 답한다. */
function failure(request, env, nonce, status, code) {
  const destination = request.headers.get("Sec-Fetch-Dest");
  if (!nonce || (destination !== "iframe" && destination !== "document")) return errorResponse(status, code);
  const body = `<!doctype html><meta charset="utf-8"><title>${status}</title><script>parent.postMessage({source:"dipsource-demo",nonce:"${nonce}",type:"error",detail:{kind:"http",status:${status},message:"${code}"}},"*")</script>`;
  const ancestors = (env.ALLOWED_FRAME_ANCESTORS ?? "").trim() || "'none'";
  return new Response(body, {
    status,
    headers: {
      "Content-Type": "text/html; charset=utf-8",
      "Content-Security-Policy": `sandbox allow-scripts; default-src 'none'; script-src 'unsafe-inline'; frame-ancestors ${ancestors}`,
      "X-Content-Type-Options": "nosniff",
      "Cache-Control": "no-store",
    },
  });
}

function staticHeaders(env, scope) {
  return new Headers({
    "Content-Security-Policy": contentSecurityPolicy(env, scope),
    "X-Content-Type-Options": "nosniff",
    "Referrer-Policy": "no-referrer",
    // sandbox 문서의 출처는 "null"이라 type=module 스크립트와 폰트를 불러오려면 CORS가 열려 있어야 한다. 자격 증명은 쓰지 않는다
    "Access-Control-Allow-Origin": "*",
    "Cross-Origin-Resource-Policy": "cross-origin",
    // 매 실행마다 공개 포인터를 다시 거치게 한다
    "Cache-Control": "no-cache",
  });
}

/**
 * HTML뿐 아니라 모든 응답에 붙인다. SVG를 주소창으로 직접 열어도 sandbox 안에서 열리게 하려는 것이다.
 * sandbox 문서에서는 'self'가 브라우저마다 다르게 해석되므로 쓰지 않고, 이 버전의 경로를 직접 적는다.
 */
function contentSecurityPolicy(env, scope) {
  const ancestors = (env.ALLOWED_FRAME_ANCESTORS ?? "").trim() || "'none'";
  return [
    "sandbox allow-scripts",
    "default-src 'none'",
    `script-src 'unsafe-inline' 'unsafe-eval' ${scope}`,
    `style-src 'unsafe-inline' ${scope}`,
    `img-src data: blob: ${scope}`,
    `font-src data: ${scope}`,
    `media-src data: blob: ${scope}`,
    `connect-src ${scope}`,
    "form-action 'none'",
    "base-uri 'none'",
    `frame-ancestors ${ancestors}`,
  ].join("; ");
}

function errorResponse(status, code, extraHeaders = {}) {
  return new Response(JSON.stringify({ error: code }), {
    status,
    headers: {
      "Content-Type": "application/json; charset=utf-8",
      "X-Content-Type-Options": "nosniff",
      "Content-Security-Policy": "sandbox; default-src 'none'",
      "Access-Control-Allow-Origin": "*",
      "Cache-Control": "no-store",
      ...extraHeaders,
    },
  });
}
