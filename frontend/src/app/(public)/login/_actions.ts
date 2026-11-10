"use server";

import { redirect } from "next/navigation";
import { hasBackend } from "@/app/_lib/api/client";
import { startMockSession } from "@/app/(user)/_lib/session";
import { isOAuthProvider } from "@/app/_lib/types/auth";
import { parseRedirect } from "@/app/_lib/redirect";

/**
 * 소셜 로그인 시작 Action.
 * - 목 모드: 가짜 세션으로 로그인/온보딩을 흉내 낸다.
 * - 실서버: github / kakao / google 모두 같은 URL 패턴으로 백엔드 OAuth를 시작한다.
 */
export async function startLogin(formData: FormData) {
  const provider = String(formData.get("provider") ?? "");
  if (!isOAuthProvider(provider)) redirect("/login");

  const target = parseRedirect(String(formData.get("redirect") ?? ""));

  // API_ORIGIN이 없으면 백엔드 연결 전 → 목 로그인
  if (!hasBackend) {
    if (provider === "kakao") redirect(`/onboarding?redirect=${encodeURIComponent(target)}`);
    await startMockSession();
    redirect(target);
  }

  // 브라우저가 이동할 백엔드 주소. (API_ORIGIN은 서버끼리 부르는 주소라 브라우저가 못 찾을 수 있다)
  // - 로컬: BROWSER_API_ORIGIN이 없으면 API_ORIGIN(http://localhost:8080)을 그대로 쓴다.
  // - Docker·배포: ""(빈 값)로 두면 같은 도메인의 /api/... 로 가고, nginx가 백엔드로 넘긴다.
  const browserApiOrigin = process.env.BROWSER_API_ORIGIN ?? process.env.API_ORIGIN;
  redirect(`${browserApiOrigin}/api/v1/auth/login/${provider}?redirect=${encodeURIComponent(target)}`);
}
