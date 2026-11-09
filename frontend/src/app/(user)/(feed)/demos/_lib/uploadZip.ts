import type { DemoPreview, DemoVersion } from "@/app/_lib/types/demo";

export type UploadResult<T> = { ok: true; data: T } | { ok: false; message: string };

const NETWORK_ERROR = "업로드하지 못했어요. 네트워크를 확인하고 다시 시도해 주세요.";

function fileRequest(file: File): RequestInit {
  const body = new FormData();
  body.append("file", file);
  return { method: "POST", body, credentials: "same-origin" };
}

function jsonRequest(payload: unknown): RequestInit {
  return {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
    credentials: "same-origin",
  };
}

/**
 * 브라우저에서 백엔드로 바로 보낸다. Server Action을 거치면 Next.js 서버가 본문을 한 번 더 메모리에 올리고 용량 제한에도 걸린다.
 * 페이지를 연 채 Access Token이 만료됐을 수 있어 401이면 한 번만 갱신하고 다시 보낸다.
 */
async function post<T>(path: string, init: RequestInit): Promise<UploadResult<T>> {
  try {
    let response = await fetch(path, init);
    if (response.status === 401) {
      const refreshed = await fetch("/api/v1/auth/refresh", { method: "POST", credentials: "same-origin" });
      if (!refreshed.ok) return { ok: false, message: "로그인이 만료됐어요. 다시 로그인해 주세요." };
      response = await fetch(path, init);
    }
    const body: unknown = await response.json().catch(() => null);
    if (!response.ok) return { ok: false, message: errorMessage(response.status, body) };
    return { ok: true, data: (body as { data: T }).data };
  } catch {
    return { ok: false, message: NETWORK_ERROR };
  }
}

export function uploadDemoZip(demoId: number, file: File) {
  return post<DemoVersion>(`/api/v1/demos/${demoId}/versions`, fileRequest(file));
}

/** 등록 전 임시 미리보기. 파일이 서버로 전송되고 만료되면 지워진다. */
export function createServerPreview(file: File) {
  return post<DemoPreview>("/api/v1/demos/previews", fileRequest(file));
}

export function uploadDemoThumbnail(demoId: number, file: File) {
  return post<string>(`/api/v1/demos/${demoId}/thumbnail`, fileRequest(file));
}

/** 코드 편집 결과를 새 버전으로 저장한다. */
export function saveEditorVersion(
  demoId: number,
  code: { html: string; css: string; js: string; mockConfig: string; sampleData: string },
) {
  return post<DemoVersion>(`/api/v1/demos/${demoId}/versions/editor`, jsonRequest(code));
}

/** Mock·샘플 데이터를 바꾼 새 버전을 만든다. */
export function saveDemoConfig(demoId: number, versionId: number, config: { mockConfig: string; sampleData: string }) {
  return post<DemoVersion>(`/api/v1/demos/${demoId}/versions/${versionId}/config`, jsonRequest(config));
}

function errorMessage(status: number, body: unknown): string {
  if (body && typeof body === "object" && "error" in body) {
    const error = (body as { error: unknown }).error;
    if (error && typeof error === "object" && "message" in error) return String((error as { message: unknown }).message);
  }
  return status === 413 ? "파일 용량이 너무 커요." : NETWORK_ERROR;
}
