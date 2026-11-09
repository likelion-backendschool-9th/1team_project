// 찍먹 데모 실행 Worker.
// 비공개 R2의 정적 파일을 내려 주기만 한다. 사용자 코드는 서버에서 실행하지 않고 브라우저의 sandbox 안에서만 돈다.

const DEMO_ROUTE = /^\/demos\/(\d{1,18})\/versions\/(\d{1,18})\/(.*)$/;
const RESERVED_ROOT = "__mock";

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
      return errorResponse(500, "internal_error");
    }
  },
};

async function route(request, env) {
  const url = new URL(request.url);
  const match = DEMO_ROUTE.exec(url.pathname);
  if (!match) return errorResponse(404, "not_found");
  const [, demoId, versionId, rawPath] = match;

  if (request.method !== "GET" && request.method !== "HEAD") {
    return errorResponse(405, "method_not_allowed", { Allow: "GET, HEAD" });
  }

  const publication = await readPublication(env, demoId);
  // 공개 포인터가 가리키는 버전만 내려 준다. 비공개·삭제된 데모와 이전 버전은 URL을 알아도 404다
  if (!publication || String(publication.versionId) !== versionId) {
    return errorResponse(404, "not_found");
  }
  const filePath = rawPath === "" ? publication.entryPath : decodeFilePath(rawPath);
  if (!filePath) return errorResponse(404, "not_found");

  return serveFile(request, env, {
    objectKey: `${publication.storagePrefix}/files/${filePath}`,
    filePath,
    scope: `${url.origin}/demos/${demoId}/versions/${versionId}/`,
  });
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

async function serveFile(request, env, { objectKey, filePath, scope }) {
  const contentType = MIME_TYPES[filePath.split(".").pop().toLowerCase()];
  if (!contentType) return errorResponse(404, "not_found");

  const headers = staticHeaders(env, scope);
  headers.set("Content-Type", contentType);

  if (request.method === "HEAD") {
    const head = await env.DEMO_BUCKET.head(objectKey);
    if (!head) return errorResponse(404, "not_found");
    headers.set("ETag", head.httpEtag);
    headers.set("Content-Length", String(head.size));
    return new Response(null, { headers });
  }

  const object = await env.DEMO_BUCKET.get(objectKey, { onlyIf: request.headers });
  if (!object) return errorResponse(404, "not_found");
  headers.set("ETag", object.httpEtag);
  // onlyIf 조건에 걸리면 본문 없는 객체가 온다 (If-None-Match 일치)
  if (!("body" in object)) return new Response(null, { status: 304, headers });
  return new Response(object.body, { headers });
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
