// 편집 중인 코드를 sandbox iframe의 srcdoc으로 합친다. 코드는 찍먹 페이지에 넣지 않고 eval도 쓰지 않는다.

export type StudioCode = {
  html: string;
  css: string;
  js: string;
  mockConfig: string;
  sampleData: string;
};

export type JsonCheck = { ok: true; value: unknown } | { ok: false; message: string };

export const MAX_MOCK_CONFIG_BYTES = 128 * 1024;
export const MAX_SAMPLE_BYTES = 256 * 1024;
const MAX_JSON_DEPTH = 16;

function depthOf(value: unknown): number {
  if (typeof value !== "object" || value === null) return 0;
  const children = Array.isArray(value) ? value : Object.values(value);
  return 1 + children.reduce<number>((max, child) => Math.max(max, depthOf(child)), 0);
}

/** 저장 전에 알려 주는 검사. 최종 검증은 백엔드가 한다. */
export function checkJson(text: string, maxBytes: number): JsonCheck {
  if (new TextEncoder().encode(text).length > maxBytes) {
    return { ok: false, message: `${Math.round(maxBytes / 1024)}KB 이하로 줄여 주세요.` };
  }
  if (!text.trim()) return { ok: true, value: null };
  let value: unknown;
  try {
    value = JSON.parse(text);
  } catch (error) {
    return { ok: false, message: `JSON 문법 오류: ${error instanceof Error ? error.message : "해석할 수 없어요"}` };
  }
  if (depthOf(value) > MAX_JSON_DEPTH) return { ok: false, message: `중첩은 ${MAX_JSON_DEPTH}단계까지 쓸 수 있어요.` };
  return { ok: true, value };
}

export type MockRow = { method: string; path: string; status: number; response: unknown };

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null && !Array.isArray(value);
}

/** 미리보기·목록 표시용으로 dataKey를 풀어 응답 표를 만든다. 형식이 틀린 항목은 건너뛴다. */
export function resolveMockRows(mockConfig: string, sampleData: string): MockRow[] {
  const config = checkJson(mockConfig, Infinity);
  const sample = checkJson(sampleData, Infinity);
  if (!config.ok || !isRecord(config.value) || !Array.isArray(config.value.endpoints)) return [];
  const data = sample.ok && isRecord(sample.value) ? sample.value : {};
  return config.value.endpoints.filter(isRecord).flatMap((endpoint) => {
    if (typeof endpoint.method !== "string" || typeof endpoint.path !== "string") return [];
    const response = typeof endpoint.dataKey === "string" ? data[endpoint.dataKey] : endpoint.response;
    return [{
      method: endpoint.method,
      path: endpoint.path,
      status: typeof endpoint.status === "number" ? endpoint.status : 200,
      response,
    }];
  });
}

const PREVIEW_CSP =
  "default-src 'none'; script-src 'unsafe-inline' 'unsafe-eval'; style-src 'unsafe-inline'; img-src data: blob:; " +
  "font-src data:; media-src data: blob:; connect-src 'none'; form-action 'none'; base-uri 'none'";

function escapeForScript(text: string): string {
  return text.replace(/<\/script/gi, "<\\/script").replace(/<!--/g, "<\\!--");
}

/** srcdoc에는 실제 Mock 주소가 없어 fetch를 표로 대신 응답한다. 저장 후에는 Worker가 응답한다. */
function mockShim(rows: MockRow[]): string {
  const table = JSON.stringify(rows).replace(/</g, "\\u003c");
  return `<script>(function(){var T=${table},F=window.fetch;
function R(b,s){return Promise.resolve(new Response(b,{status:s,headers:{"content-type":"application/json"}}))}
window.fetch=function(i,o){var u=typeof i==="string"?i:(i&&i.url)||String(i),m=/(?:^|\\/)__mock(\\/[^?#]*)/.exec(u);
if(!m)return F.apply(this,arguments);
var M=((o&&o.method)||(i&&i.method)||"GET").toUpperCase(),a=T.filter(function(x){return x.path===m[1]});
if(!a.length)return R('{"error":"not_found"}',404);
var h=a.filter(function(x){return x.method===M})[0];
if(!h)return R('{"error":"method_not_allowed"}',405);
return R(h.status===204?null:JSON.stringify(h.response===undefined?null:h.response),h.status)}})();</script>`;
}

/** 편집 내용을 실행 가능한 문서 하나로 합친다. */
export function composeStudioDocument(code: StudioCode): string {
  const head = [
    `<meta http-equiv="Content-Security-Policy" content="${PREVIEW_CSP}">`,
    mockShim(resolveMockRows(code.mockConfig, code.sampleData)),
    `<style>${code.css.replace(/<\/style/gi, "<\\/style")}</style>`,
  ].join("\n");
  const script = `<script>${escapeForScript(code.js)}</script>`;

  if (!/<html[\s>]/i.test(code.html)) {
    return `<!doctype html><html lang="ko"><head><meta charset="utf-8">${head}</head><body>${code.html}\n${script}</body></html>`;
  }
  const withHead = /<head[^>]*>/i.test(code.html)
    ? code.html.replace(/<head[^>]*>/i, (tag) => `${tag}\n${head}`)
    : code.html.replace(/<html[^>]*>/i, (tag) => `${tag}<head>${head}</head>`);
  const closing = withHead.toLowerCase().lastIndexOf("</body>");
  return closing < 0 ? `${withHead}\n${script}` : `${withHead.slice(0, closing)}${script}\n${withHead.slice(closing)}`;
}
