"use server";

import { redirect } from "next/navigation";
import { isMockMode } from "@/app/_lib/api/client";
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
  if (isMockMode) {
    if (provider === "kakao") redirect(`/onboarding?redirect=${encodeURIComponent(target)}`);
    await startMockSession();
    redirect(target);
  }

  // Next /api 프록시가 아니라 백엔드로 직접 보낸다.
  // (상대 경로 리다이렉트가 localhost:3000으로 깨지는 것을 막기 위함)
  // provider만 바꾸면 카카오·구글도 같은 흐름을 탄다.
  const apiOrigin = process.env.API_ORIGIN;
  redirect(`${apiOrigin}/api/v1/auth/login/${provider}?redirect=${encodeURIComponent(target)}`);
}
