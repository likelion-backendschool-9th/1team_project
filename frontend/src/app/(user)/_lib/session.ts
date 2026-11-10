import "server-only";
import { cookies } from "next/headers";
import { hasBackend } from "@/app/_lib/api/client";

const ACCESS_TOKEN_COOKIE = "access_token";
const REFRESH_TOKEN_COOKIE = "refresh_token";
const MOCK_SESSION_COOKIE = "mock_session";

/**
 * 로그인 여부.
 * - 목 모드: mock_session 쿠키로 판단 (없으면 MOCK_GUEST가 아닐 때 로그인으로 본다)
 * - 실서버: 백엔드가 심은 access_token 쿠키 존재 여부
 */
export async function isLoggedIn(): Promise<boolean> {
  const store = await cookies();
  if (!hasBackend) {
    const mock = store.get(MOCK_SESSION_COOKIE)?.value;
    if (mock) return mock === "user";
    return process.env.MOCK_GUEST !== "1";
  }
  return store.has(ACCESS_TOKEN_COOKIE);
}

/** 목 모드 로그인. 백엔드 JWT 대신 가짜 세션 쿠키를 심는다. */
export async function startMockSession(): Promise<void> {
  (await cookies()).set(MOCK_SESSION_COOKIE, "user", { path: "/", httpOnly: true, sameSite: "lax" });
}

/** 로그아웃 시 프론트가 들고 있는 인증 쿠키를 지운다. */
export async function endSession(): Promise<void> {
  const store = await cookies();
  if (!hasBackend) {
    store.set(MOCK_SESSION_COOKIE, "guest", { path: "/", httpOnly: true, sameSite: "lax" });
    return;
  }
  store.delete(ACCESS_TOKEN_COOKIE);
  store.delete(REFRESH_TOKEN_COOKIE);
}
