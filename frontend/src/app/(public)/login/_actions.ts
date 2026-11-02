"use server";

import { redirect } from "next/navigation";
import { isMockMode } from "@/app/_lib/api/client";
import { isOAuthProvider } from "@/app/_lib/types/auth";
import { parseRedirect } from "@/app/_lib/redirect";

// 소셜 로그인 시작. 목 모드에서는 인증 없이 돌아갈 곳으로 보낸다.
// TODO(백엔드 연동): 기획서의 auth/login/{provider} 명세에 맞춰 교체한다.
export async function startLogin(formData: FormData) {
  const provider = String(formData.get("provider") ?? "");
  if (!isOAuthProvider(provider)) redirect("/login");

  const target = parseRedirect(String(formData.get("redirect") ?? ""));
  if (isMockMode) redirect(target);
  redirect(`/api/v1/auth/login/${provider}?redirect=${encodeURIComponent(target)}`);
}
