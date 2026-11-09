// ZIP을 서버로 보내지 않고 브라우저 안에서 풀어 미리보기를 만든다.
// srcdoc 하나로 재현할 수 있는 단순한 프로젝트만 대상으로 한다. 멀티파일 모듈 번들은 상대 import를 재현할 수 없어 서버 미리보기로 넘긴다.

const END_OF_CENTRAL = 0x06054b50;
const CENTRAL_HEADER = 0x02014b50;
const LOCAL_HEADER = 0x04034b50;
const MAX_LOCAL_FILES = 30;
const MAX_TOTAL_BYTES = 30 * 1024 * 1024;

export type ZipFileEntry = { path: string; bytes: Uint8Array };

export type PreviewPlan =
  | { mode: "local"; files: Map<string, Uint8Array> }
  | { mode: "server"; reason: string };

function findEndOfCentral(view: DataView): number {
  for (let at = view.byteLength - 22; at >= Math.max(0, view.byteLength - 65557); at--) {
    if (view.getUint32(at, true) === END_OF_CENTRAL) return at;
  }
  return -1;
}

async function inflate(data: Uint8Array<ArrayBuffer>, limit: number): Promise<Uint8Array> {
  const reader = new Blob([data]).stream().pipeThrough(new DecompressionStream("deflate-raw")).getReader();
  const chunks: Uint8Array[] = [];
  let total = 0;
  for (;;) {
    const { done, value } = await reader.read();
    if (done) break;
    total += value.byteLength;
    if (total > limit) {
      await reader.cancel();
      throw new Error("too large");
    }
    chunks.push(value);
  }
  const merged = new Uint8Array(total);
  let offset = 0;
  for (const chunk of chunks) {
    merged.set(chunk, offset);
    offset += chunk.byteLength;
  }
  return merged;
}

/** ZIP을 푼다. 실제로 푼 크기로 한도를 검사한다. 읽을 수 없으면 null. */
export async function readZip(buffer: ArrayBuffer): Promise<ZipFileEntry[] | null> {
  try {
    const view = new DataView(buffer);
    const end = findEndOfCentral(view);
    if (end < 0) return null;
    const count = view.getUint16(end + 10, true);
    let at = view.getUint32(end + 16, true);
    const decoder = new TextDecoder("utf-8", { fatal: true });
    const entries: ZipFileEntry[] = [];
    let total = 0;

    for (let index = 0; index < count; index++) {
      if (view.getUint32(at, true) !== CENTRAL_HEADER) return null;
      const method = view.getUint16(at + 10, true);
      const compressedSize = view.getUint32(at + 20, true);
      const nameLength = view.getUint16(at + 28, true);
      const extraLength = view.getUint16(at + 30, true);
      const commentLength = view.getUint16(at + 32, true);
      const localOffset = view.getUint32(at + 42, true);
      const path = decoder.decode(new Uint8Array(buffer, at + 46, nameLength)).normalize("NFC");
      at += 46 + nameLength + extraLength + commentLength;

      if (path.endsWith("/") || path.startsWith("__MACOSX/") || path.endsWith(".DS_Store")) continue;
      if (view.getUint32(localOffset, true) !== LOCAL_HEADER) return null;
      const dataStart = localOffset + 30 + view.getUint16(localOffset + 26, true) + view.getUint16(localOffset + 28, true);
      const raw = new Uint8Array(buffer, dataStart, compressedSize);
      if (method !== 0 && method !== 8) return null;
      const bytes = method === 8 ? await inflate(raw, MAX_TOTAL_BYTES - total) : raw;
      total += bytes.byteLength;
      if (total > MAX_TOTAL_BYTES) return null;
      entries.push({ path, bytes });
    }
    return entries;
  } catch {
    return null;
  }
}

const utf8 = new TextDecoder();
const MODULE_SCRIPT = /<script\b[^>]*\btype\s*=\s*["']?module/i;
const JS_REQUIRES_SERVER = /\bimport\s*\(|^\s*import\s[^;]*from\s|\bfetch\s*\(|\bXMLHttpRequest\b|\bnew\s+Worker\s*\(/m;

/** 브라우저 안에서 재현할 수 있는지 판단한다. */
export function planPreview(entries: ZipFileEntry[]): PreviewPlan {
  const files = new Map(entries.map((entry) => [entry.path, entry.bytes]));
  const html = files.get("index.html");
  if (!html) return { mode: "server", reason: "index.html을 찾지 못했어요." };
  if (files.size > MAX_LOCAL_FILES) return { mode: "server", reason: "파일이 많아 브라우저에서 재현하기 어려워요." };
  if (MODULE_SCRIPT.test(utf8.decode(html))) {
    return { mode: "server", reason: "ES 모듈 번들(React·Vue 빌드 등)은 상대 경로 import를 브라우저만으로 재현할 수 없어요." };
  }
  for (const [path, bytes] of files) {
    if (/\.m?js$/i.test(path) && JS_REQUIRES_SERVER.test(utf8.decode(bytes))) {
      return { mode: "server", reason: "모듈 import나 네트워크 요청을 쓰는 스크립트가 있어요." };
    }
  }
  return { mode: "local", files };
}

const MIME_BY_EXTENSION: Record<string, string> = {
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

function toDataUrl(path: string, bytes: Uint8Array): string | null {
  const mime = MIME_BY_EXTENSION[path.split(".").pop()?.toLowerCase() ?? ""];
  if (!mime) return null;
  let binary = "";
  for (let at = 0; at < bytes.length; at += 0x8000) {
    binary += String.fromCharCode(...bytes.subarray(at, at + 0x8000));
  }
  return `data:${mime};base64,${btoa(binary)}`;
}

/** 문서 기준 상대 경로를 ZIP 안의 경로로 바꾼다. 외부 주소면 null. */
export function resolveLocal(base: string, reference: string): string | null {
  if (!reference || /^[a-z][a-z0-9+.-]*:|^\/\/|^#/i.test(reference)) return null;
  const clean = reference.split(/[?#]/)[0];
  const stack = clean.startsWith("/") ? [] : base.split("/").slice(0, -1);
  for (const segment of clean.split("/")) {
    if (segment === "" || segment === ".") continue;
    if (segment === "..") stack.pop();
    else stack.push(segment);
  }
  return stack.join("/");
}

function inlineCssUrls(css: string, cssPath: string, files: Map<string, Uint8Array>): string {
  return css.replace(/url\(\s*(["']?)([^"')]+)\1\s*\)/g, (match, _quote: string, reference: string) => {
    const path = resolveLocal(cssPath, reference);
    const bytes = path === null ? undefined : files.get(path);
    const dataUrl = path !== null && bytes ? toDataUrl(path, bytes) : null;
    return dataUrl ? `url("${dataUrl}")` : match;
  });
}

// 데모 실행 환경과 같게 외부 통신을 막는다. iframe의 sandbox 속성과 함께 쓴다
const LOCAL_CSP =
  "default-src 'none'; script-src 'unsafe-inline' 'unsafe-eval'; style-src 'unsafe-inline'; img-src data: blob:; " +
  "font-src data:; media-src data: blob:; connect-src 'none'; form-action 'none'; base-uri 'none'";

/** 외부 파일을 모두 인라인해 srcdoc 하나로 만든다. DOMParser가 필요해 브라우저에서만 호출한다. */
export function buildSrcdoc(files: Map<string, Uint8Array>): string {
  const doc = new DOMParser().parseFromString(utf8.decode(files.get("index.html")), "text/html");

  doc.querySelectorAll<HTMLLinkElement>('link[rel~="stylesheet"][href]').forEach((link) => {
    const path = resolveLocal("index.html", link.getAttribute("href") ?? "");
    const bytes = path === null ? undefined : files.get(path);
    if (path === null || !bytes) return;
    const style = doc.createElement("style");
    style.textContent = inlineCssUrls(utf8.decode(bytes), path, files);
    link.replaceWith(style);
  });

  doc.querySelectorAll("script[src]").forEach((script) => {
    const path = resolveLocal("index.html", script.getAttribute("src") ?? "");
    const bytes = path === null ? undefined : files.get(path);
    if (!bytes) return;
    const inline = doc.createElement("script");
    inline.textContent = utf8.decode(bytes).replace(/<\/script/gi, "<\\/script");
    script.replaceWith(inline);
  });

  doc.querySelectorAll("img[src], source[src], link[rel~='icon'][href]").forEach((element) => {
    const attribute = element.hasAttribute("src") ? "src" : "href";
    const path = resolveLocal("index.html", element.getAttribute(attribute) ?? "");
    const bytes = path === null ? undefined : files.get(path);
    const dataUrl = path !== null && bytes ? toDataUrl(path, bytes) : null;
    if (dataUrl) element.setAttribute(attribute, dataUrl);
  });

  const csp = doc.createElement("meta");
  csp.httpEquiv = "Content-Security-Policy";
  csp.content = LOCAL_CSP;
  doc.head.prepend(csp);
  return `<!doctype html>${doc.documentElement.outerHTML}`;
}
