import "server-only";
import { cookies } from "next/headers";
import { isMockMode } from "@/app/_lib/api/client";

const ACCESS_TOKEN_COOKIE = "access_token";
const MOCK_SESSION_COOKIE = "mock_session";

// 로그인 여부. 목 모드에서는 mock_session 쿠키로 판단하고, 쿠키가 없으면 기본은 로그인 상태다.
// 쿠키가 없을 때 MOCK_GUEST=1이면 비로그인 화면을 볼 수 있다.
export async function isLoggedIn(): Promise<boolean> {
  const store = await cookies();
  if (isMockMode) {
    const mock = store.get(MOCK_SESSION_COOKIE)?.value;
    if (mock) return mock === "user";
    return process.env.MOCK_GUEST !== "1";
  }
  return store.has(ACCESS_TOKEN_COOKIE);
}

// 목 모드 로그인. 백엔드가 토큰 쿠키를 내려 주기 전까지 로그인 상태를 흉내 낸다.
export async function startMockSession(): Promise<void> {
  (await cookies()).set(MOCK_SESSION_COOKIE, "user", { path: "/", httpOnly: true, sameSite: "lax" });
}

export async function endSession(): Promise<void> {
  const store = await cookies();
  if (isMockMode) store.set(MOCK_SESSION_COOKIE, "guest", { path: "/", httpOnly: true, sameSite: "lax" });
  else store.delete(ACCESS_TOKEN_COOKIE);
}
