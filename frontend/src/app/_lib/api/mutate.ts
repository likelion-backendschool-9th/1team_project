import "server-only";
import { apiFetch, ApiRequestError, isMockMode } from "./client";

export type MutationResult = { ok: boolean; message: string };

// 변경 요청 공통 처리. 목 모드에서는 성공으로 응답하고 저장되지 않았다는 안내를 붙인다.
// useMock: 백엔드가 이미 있는 기능은 !hasBackend를 넘겨 MOCK_EXCEPT_DM이어도 실서버를 쓴다.
export async function mutate(
  path: `/api/v1/${string}`,
  method: "POST" | "PUT" | "DELETE",
  body: unknown,
  successMessage: string,
  useMock = isMockMode,
): Promise<MutationResult> {
  if (useMock)
    return { ok: true, message: `${successMessage} (백엔드 연결 전이라 실제로 저장되지는 않아요)` };

  try {
    await apiFetch(path, {
      method,
      body: body === undefined ? undefined : JSON.stringify(body),
    });
  } catch (error) {
    if (error instanceof ApiRequestError) return { ok: false, message: error.error.message };
    throw error;
  }
  return { ok: true, message: successMessage };
}
