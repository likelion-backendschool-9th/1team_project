import type { DemoPreview, DemoVersion } from "@/app/_lib/types/demo";

export type UploadResult<T> = { ok: true; data: T } | { ok: false; message: string };

const NETWORK_ERROR = "업로드하지 못했어요. 네트워크를 확인하고 다시 시도해 주세요.";

function sendFile(path: string, file: File): Promise<Response> {
  const body = new FormData();
  body.append("file", file);
  return fetch(path, { method: "POST", body, credentials: "same-origin" });
}

/**
 * 파일을 브라우저에서 백엔드로 바로 보낸다. Server Action을 거치면 Next.js 서버가 파일을 한 번 더 메모리에 올린다.
 * 페이지를 연 채 Access Token이 만료됐을 수 있어 401이면 한 번만 갱신하고 다시 보낸다.
 */
async function postFile<T>(path: string, file: File): Promise<UploadResult<T>> {
  try {
    let response = await sendFile(path, file);
    if (response.status === 401) {
      const refreshed = await fetch("/api/v1/auth/refresh", { method: "POST", credentials: "same-origin" });
      if (!refreshed.ok) return { ok: false, message: "로그인이 만료됐어요. 다시 로그인해 주세요." };
      response = await sendFile(path, file);
    }
    const body: unknown = await response.json().catch(() => null);
    if (!response.ok) return { ok: false, message: errorMessage(response.status, body) };
    return { ok: true, data: (body as { data: T }).data };
  } catch {
    return { ok: false, message: NETWORK_ERROR };
  }
}

export function uploadDemoZip(demoId: number, file: File) {
  return postFile<DemoVersion>(`/api/v1/demos/${demoId}/versions`, file);
}

/** 등록 전 임시 미리보기. 파일이 서버로 전송되고 만료되면 지워진다. */
export function createServerPreview(file: File) {
  return postFile<DemoPreview>("/api/v1/demos/previews", file);
}

export function uploadDemoThumbnail(demoId: number, file: File) {
  return postFile<string>(`/api/v1/demos/${demoId}/thumbnail`, file);
}

function errorMessage(status: number, body: unknown): string {
  if (body && typeof body === "object" && "error" in body) {
    const error = (body as { error: unknown }).error;
    if (error && typeof error === "object" && "message" in error) return String((error as { message: unknown }).message);
  }
  return status === 413 ? "파일 용량이 너무 커요." : NETWORK_ERROR;
}
