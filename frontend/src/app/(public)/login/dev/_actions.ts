"use server";

import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import { hasBackend } from "@/app/_lib/api/client";
import { DEV_LOGIN_ACCOUNTS } from "./_constants";

type DevLoginData = {
  accessToken: string;
  refreshToken: string;
  userId: number;
  nickname: string;
  email: string;
};

/**
 * TEMP-DEV-LOGIN: 백엔드에서 JWT를 받아 Next 쿠키에 심는다.
 * (httpOnly라 브라우저에서 직접 넣을 수 없음)
 */
export async function loginAsDevAccount(formData: FormData) {
  if (!hasBackend) redirect("/login");

  const email = String(formData.get("email") ?? "");
  const allowed = DEV_LOGIN_ACCOUNTS.some((account) => account.email === email);
  if (!allowed) redirect("/login/dev");

  const origin = process.env.API_ORIGIN;
  if (!origin) redirect("/login");

  const response = await fetch(`${origin}/api/v1/auth/dev-login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email }),
    cache: "no-store",
  });

  if (!response.ok) {
    redirect(`/login/dev?error=${response.status}`);
  }

  const body = (await response.json()) as { data: DevLoginData };
  const { accessToken, refreshToken } = body.data;
  const store = await cookies();

  store.set("access_token", accessToken, {
    httpOnly: true,
    sameSite: "lax",
    path: "/",
    maxAge: 60 * 30,
  });
  store.set("refresh_token", refreshToken, {
    httpOnly: true,
    sameSite: "lax",
    path: "/",
    maxAge: 60 * 60 * 24 * 14,
  });

  redirect("/");
}
