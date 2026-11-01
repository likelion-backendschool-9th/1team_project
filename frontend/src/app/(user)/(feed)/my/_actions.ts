"use server";

import { runForm } from "@/app/_lib/api/formAction";
import { mutate } from "@/app/_lib/api/mutate";
import { readValues, type FormState } from "@/app/_lib/formState";

function isId(value: number) {
  return Number.isSafeInteger(value) && value > 0;
}

export async function saveAccount(previous: FormState, formData: FormData): Promise<FormState> {
  const values = readValues(formData, ["nickname", "bio", "techStacks", "githubUrl"]);
  const errors: Record<string, string> = {};
  if (!values.nickname) errors.nickname = "닉네임을 입력해 주세요.";
  else if (values.nickname.length > 12) errors.nickname = "12자 이하로 입력해 주세요.";
  if (values.bio.length > 150) errors.bio = "150자 이하로 입력해 주세요.";
  if (values.githubUrl && !/^https:\/\/github\.com\/[\w-]+\/?$/.test(values.githubUrl))
    errors.githubUrl = "https://github.com/아이디 형식으로 입력해 주세요.";

  return runForm(previous, values, errors, () =>
    mutate("/api/v1/users/me", "PUT", values, "내 정보를 저장했어요."),
  );
}

export async function saveSettings(previous: FormState, formData: FormData): Promise<FormState> {
  const keys = ["like", "comment", "follow", "dm", "guestbook"];
  const notify = Object.fromEntries(keys.map((key) => [key, formData.get(`notify.${key}`) === "on"]));
  const values = readValues(formData, ["visibility"]);
  const body = { notify, visibility: values.visibility, allowDm: formData.get("allowDm") === "on" };
  return runForm(previous, values, {}, () =>
    mutate("/api/v1/users/me/settings", "PUT", body, "설정을 저장했어요."),
  );
}

export async function unblockUser(userId: number) {
  if (!isId(userId)) return { ok: false, message: "잘못된 요청이에요." };
  return mutate(`/api/v1/blocks/${userId}`, "DELETE", undefined, "차단을 해제했어요.");
}

export async function purchaseChips(packageId: number) {
  if (!isId(packageId)) return { ok: false, message: "잘못된 요청이에요." };
  return mutate("/api/v1/coins/purchases", "POST", { packageId }, "칩을 충전했어요.");
}

export async function cancelSubscription(subscriptionId: number) {
  if (!isId(subscriptionId)) return { ok: false, message: "잘못된 요청이에요." };
  return mutate("/api/v1/subscriptions/me", "DELETE", { subscriptionId }, "구독을 해지했어요. 다음 결제일부터 청구되지 않아요.");
}

export async function logout() {
  return mutate("/api/v1/auth/logout", "POST", undefined, "로그아웃했어요.");
}

export async function withdraw() {
  return mutate("/api/v1/users/me", "DELETE", undefined, "탈퇴 요청을 접수했어요.");
}
