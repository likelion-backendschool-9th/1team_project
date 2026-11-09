import type { DemoVersion } from "@/app/_lib/types/demo";

export type UploadResult =
  | { ok: true; version: DemoVersion }
  | { ok: false; message: string };

const NETWORK_ERROR = "업로드하지 못했어요. 네트워크를 확인하고 다시 시도해 주세요.";

async function send(demoId: number, file: File): Promise<Response> {
  const body = new FormData();
  body.append("file", file);
  return fetch(`/api/v1/demos/${demoId}/versions`, { method: "POST", body, credentials: "same-origin" });
}

/**
 * ZIP을 브라우저에서 백엔드로 바로 보낸다. Server Action을 거치면 Next.js 서버가 파일을 한 번 더 메모리에 올린다.
 * 페이지를 연 채 Access Token이 만료됐을 수 있어 401이면 한 번만 갱신하고 다시 보낸다.
 */
export async function uploadDemoZip(demoId: number, file: File): Promise<UploadResult> {
  try {
    let response = await send(demoId, file);
    if (response.status === 401) {
      const refreshed = await fetch("/api/v1/auth/refresh", { method: "POST", credentials: "same-origin" });
      if (!refreshed.ok) return { ok: false, message: "로그인이 만료됐어요. 다시 로그인해 주세요." };
      response = await send(demoId, file);
    }
    const body: unknown = await response.json().catch(() => null);
    if (!response.ok) return { ok: false, message: errorMessage(response.status, body) };
    return { ok: true, version: (body as { data: DemoVersion }).data };
  } catch {
    return { ok: false, message: NETWORK_ERROR };
  }
}

function errorMessage(status: number, body: unknown): string {
  if (body && typeof body === "object" && "error" in body) {
    const error = (body as { error: unknown }).error;
    if (error && typeof error === "object" && "message" in error) return String((error as { message: unknown }).message);
  }
  return status === 413 ? "파일 용량이 너무 커요." : NETWORK_ERROR;
}
