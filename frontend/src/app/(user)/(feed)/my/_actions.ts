"use server";

import { redirect } from "next/navigation";
import { revalidatePath } from "next/cache";
import { hasBackend } from "@/app/_lib/api/client";
import { runForm } from "@/app/_lib/api/formAction";
import { mutate } from "@/app/_lib/api/mutate";
import { readValues, type FormState } from "@/app/_lib/formState";
import { endSession } from "@/app/(user)/_lib/session";
import { isOAuthProvider, type OAuthProvider } from "@/app/_lib/types/auth";

function isId(value: number) {
  return Number.isSafeInteger(value) && value > 0;
}

export async function saveAccount(previous: FormState, formData: FormData): Promise<FormState> {
  const values = readValues(formData, ["nickname", "bio", "techStacks", "githubUrl"]);
  const errors: Record<string, string> = {};
  if (!values.nickname) errors.nickname = "닉네임을 입력해 주세요.";
  else if (values.nickname.length > 12) errors.nickname = "12자 이하로 입력해 주세요.";
  if (values.bio.length > 200) errors.bio = "200자 이하로 입력해 주세요.";
  if (values.githubUrl && !/^https:\/\/github\.com\/[\w-]+\/?$/.test(values.githubUrl))
    errors.githubUrl = "https://github.com/아이디 형식으로 입력해 주세요.";

  return runForm(previous, values, errors, () =>
    mutate("/api/v1/users/me", "PUT", values, "내 정보를 저장했어요.", !hasBackend),
  );
}

export async function saveSettings(previous: FormState, formData: FormData): Promise<FormState> {
  const keys = ["like", "comment", "follow", "dm", "guestbook"];
  const notify = Object.fromEntries(keys.map((key) => [key, formData.get(`notify.${key}`) === "on"]));
  const values = readValues(formData, ["visibility"]);
  const body = { notify, visibility: values.visibility, allowDm: formData.get("allowDm") === "on" };
  return runForm(previous, values, {}, () =>
    mutate("/api/v1/users/me/settings", "PUT", body, "설정을 저장했어요.", !hasBackend),
  );
}

export async function unblockUser(userId: number) {
  if (!isId(userId)) return { ok: false, message: "잘못된 요청이에요." };
  const result = await mutate(`/api/v1/blocks/${userId}`, "DELETE", undefined, "차단을 해제했어요.", !hasBackend);
  if (result.ok) revalidatePath("/my/settings");
  return result;
}

export async function purchaseChips(packageId: number) {
  if (!isId(packageId)) return { ok: false, message: "잘못된 요청이에요." };
  const result = await mutate("/api/v1/coins/purchases", "POST", { packageId }, "칩을 충전했어요.");
  if (result.ok) {
    // 잔액·거래 내역 갱신
    revalidatePath("/my/coins");
    revalidatePath("/my");
  }
  return result;
}

export async function subscribePlus() {
  const result = await mutate("/api/v1/subscriptions", "POST", { plan: "plus" }, "딥소스 플러스를 시작했어요. 매달 자동이체돼요.");
  if (result.ok) revalidatePath("/", "layout");
  return result;
}

export async function cancelPlus() {
  const result = await mutate("/api/v1/subscriptions/me", "DELETE", { plan: "plus" }, "딥소스 플러스를 해지했어요. 이번 결제 기간까지는 혜택이 유지돼요.");
  if (result.ok) revalidatePath("/", "layout");
  return result;
}

export async function verifyCompany() {
  return { ok: false, message: "회사 인증은 준비 중이에요. 조금만 기다려 주세요." };
}

/** 마이페이지 로그아웃. 백엔드 쿠키 만료 + 로컬 세션 정리를 한다. */
export async function logout() {
  const result = await mutate("/api/v1/auth/logout", "POST", undefined, "로그아웃했어요.");
  await endSession();
  return result;
}

export async function withdraw() {
  const result = await mutate("/api/v1/users/me", "DELETE", undefined, "회원탈퇴가 완료되었습니다. 그동안 서비스를 이용해주셔서 감사드립니다.", !hasBackend);
  if (!result.ok) return result;
  await endSession();
  redirect("/goodbye");
}

export async function unlinkSocialAccount(provider: OAuthProvider) {
  if (!isOAuthProvider(provider)) return { ok: false, message: "잘못된 요청이에요." };
  const result = await mutate(
    `/api/v1/users/me/social-accounts/${provider}`,
    "DELETE",
    undefined,
    "연동을 해제했어요.",
    !hasBackend,
  );
  if (result.ok) revalidatePath("/my");
  return result;
}
