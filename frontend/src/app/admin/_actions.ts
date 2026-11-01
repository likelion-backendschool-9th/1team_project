"use server";

import { revalidatePath } from "next/cache";
import { runForm } from "@/app/_lib/api/formAction";
import { mutate } from "@/app/_lib/api/mutate";
import { readValues, type FormState } from "@/app/_lib/formState";
import type { ContentKind } from "./_lib/mock";

// 관리자 권한 확인은 백엔드 인증이 붙으면 각 액션 첫 줄에서 한다 (FRONTEND.md 8-3).

function isId(value: number) {
  return Number.isSafeInteger(value) && value > 0;
}

const KINDS: ContentKind[] = ["demos", "posts", "feeds", "comments"];
const PERIODS = ["1일", "7일", "30일", "영구"];
const DECISIONS = ["hide", "sanction", "reject"];

export async function sanctionUser(userId: number, previous: FormState, formData: FormData): Promise<FormState> {
  const values = readValues(formData, ["period", "reason"]);
  const errors: Record<string, string> = {};
  if (!PERIODS.includes(values.period)) errors.period = "제재 기간을 골라 주세요.";
  if (values.reason.length < 5) errors.reason = "사유를 5자 이상 입력해 주세요.";
  const state = await runForm(previous, values, errors, () =>
    mutate(`/api/v1/admin/users/${userId}/ban`, "PUT", values, `${values.period} 제재를 적용했어요.`),
  );
  if (state.ok) revalidatePath(`/admin/users/${userId}`);
  return state;
}

export async function releaseUser(userId: number) {
  if (!isId(userId)) return { ok: false, message: "잘못된 요청이에요." };
  return mutate(`/api/v1/admin/users/${userId}/ban`, "DELETE", undefined, "제재를 해제했어요.");
}

export async function deleteContent(kind: ContentKind, id: number) {
  if (!KINDS.includes(kind) || !isId(id)) return { ok: false, message: "잘못된 요청이에요." };
  return mutate(`/api/v1/admin/${kind}/${id}`, "DELETE", undefined, "삭제했어요.");
}

export async function processReport(reportId: number, previous: FormState, formData: FormData): Promise<FormState> {
  const values = readValues(formData, ["decision", "memo"]);
  const errors: Record<string, string> = {};
  if (!DECISIONS.includes(values.decision)) errors.decision = "처리 방식을 골라 주세요.";
  if (values.memo.length < 5) errors.memo = "처리 사유를 5자 이상 입력해 주세요.";
  const state = await runForm(previous, values, errors, () =>
    mutate(`/api/v1/admin/reports/${reportId}`, "PUT", values, "신고를 처리했어요."),
  );
  if (state.ok) revalidatePath("/admin/reports");
  return state;
}

export async function confirmSettlement(settlementId: number) {
  if (!isId(settlementId)) return { ok: false, message: "잘못된 요청이에요." };
  return mutate(`/api/v1/admin/settlements/${settlementId}/confirm`, "PUT", undefined, "정산을 확정했어요.");
}

export async function approveRefund(txId: number) {
  if (!isId(txId)) return { ok: false, message: "잘못된 요청이에요." };
  return mutate(`/api/v1/admin/coins/purchases/${txId}/refund`, "PUT", undefined, "환불을 승인했어요.");
}

// 부정 사용 계정은 회원 제재 API로 막는다.
export async function suspendForFraud(userId: number) {
  if (!isId(userId)) return { ok: false, message: "잘못된 요청이에요." };
  return mutate(
    `/api/v1/admin/users/${userId}/ban`,
    "PUT",
    { period: "영구", reason: "결제 부정 사용" },
    "계정을 정지했어요.",
  );
}
