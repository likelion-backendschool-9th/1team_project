import "server-only";
import { cookies } from "next/headers";
import { isMockMode } from "@/app/_lib/api/client";

const ACCESS_TOKEN_COOKIE = "access_token";

// 로그인 여부. 목 모드에서는 기본이 로그인 상태이고, MOCK_GUEST=1이면 비로그인 화면을 볼 수 있다.
export async function isLoggedIn(): Promise<boolean> {
  if (isMockMode) return process.env.MOCK_GUEST !== "1";
  return (await cookies()).has(ACCESS_TOKEN_COOKIE);
}
