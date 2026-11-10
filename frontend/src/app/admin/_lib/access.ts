import "server-only";
import { apiFetch, ApiRequestError, hasBackend, type ApiResponse } from "@/app/_lib/api/client";

// master: 최고 관리자(관리자 기능 + 권한 관리), admin: 관리자
export type AdminAccess = "master" | "admin" | "guest" | "forbidden";

// 관리자 화면 입장 확인. 실제 권한 검사는 백엔드(/admin/** hasRole ADMIN)가 하고,
// 여기서는 권한 없는 사람에게 관리 화면 자체를 보여 주지 않으려고 내 정보(GET users/me)의 role을 본다.
// 백엔드 연결 전(목 데이터)에는 화면을 모두 확인할 수 있게 마스터로 본다.
export async function getAdminAccess(): Promise<AdminAccess> {
  if (!hasBackend) return "master";
  try {
    const { data } = await apiFetch<ApiResponse<{ role?: string }>>("/api/v1/users/me");
    if (data.role === "MASTER") return "master";
    return data.role === "ADMIN" ? "admin" : "forbidden";
  } catch (error) {
    if (error instanceof ApiRequestError && error.status === 401) return "guest";
    throw error;
  }
}
