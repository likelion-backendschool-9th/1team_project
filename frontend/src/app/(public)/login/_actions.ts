"use server";

import { redirect } from "next/navigation";
import { isMockMode } from "@/app/_lib/api/client";
import { startMockSession } from "@/app/(user)/_lib/session";
import { isOAuthProvider } from "@/app/_lib/types/auth";
import { parseRedirect } from "@/app/_lib/redirect";

// 소셜 로그인 시작. 목 모드에서는 카카오를 신규 가입(온보딩)으로, 나머지는 기존 회원 로그인으로 흉내 낸다.
// TODO(백엔드 연동): 기획서의 auth/login/{provider} 명세에 맞춰 교체한다.
export async function startLogin(formData: FormData) {
  const provider = String(formData.get("provider") ?? "");
  if (!isOAuthProvider(provider)) redirect("/login");

  const target = parseRedirect(String(formData.get("redirect") ?? ""));
  if (isMockMode) {
    if (provider === "kakao") redirect(`/onboarding?redirect=${encodeURIComponent(target)}`);
    await startMockSession();
    redirect(target);
  }
  redirect(`/api/v1/auth/login/${provider}?redirect=${encodeURIComponent(target)}`);
}
