"use server";

import { revalidatePath } from "next/cache";
import { apiFetch, ApiRequestError, hasBackend, type ApiResponse } from "@/app/_lib/api/client";
import { runForm } from "@/app/_lib/api/formAction";
import { mutate, type MutationResult } from "@/app/_lib/api/mutate";
import { readValues, type FormState } from "@/app/_lib/formState";
import type { DemoDetail } from "@/app/_lib/types/demo";
import { getMe } from "@/app/(user)/_lib/api";
import { MAX_DESCRIPTION, MAX_TITLE, parseTags } from "./upload/_lib/rules";

const NO_BACKEND = "백엔드 연결 전이라 저장할 수 없어요.";

function revalidateDemo(demoId: number) {
  revalidatePath("/demos");
  revalidatePath(`/demos/${demoId}/manage`);
  revalidatePath("/mini-home/[userId]/demos/[demoId]", "page");
  revalidatePath("/mini-home/[userId]/demos", "page");
}

function validateInfo(title: string, description: string): Record<string, string> {
  const errors: Record<string, string> = {};
  if (!title) errors.title = "데모 이름을 입력해 주세요.";
  else if (title.length > MAX_TITLE) errors.title = `${MAX_TITLE}자 이하로 입력해 주세요.`;
  if (description.length > MAX_DESCRIPTION) errors.description = `${MAX_DESCRIPTION}자 이하로 입력해 주세요.`;
  return errors;
}

export type CreateDraftInput = { title: string; description: string; tags: string };
export type CreateDraftResult = MutationResult & { demoId?: number; errors?: Record<string, string> };

/** 초안을 만든다. 파일은 브라우저가 이어서 버전 API로 직접 올린다 (multipart는 Server Action 용량 제한에 걸린다). */
export async function createDemoDraft(input: CreateDraftInput): Promise<CreateDraftResult> {
  if (!(await getMe())) return { ok: false, message: "로그인이 필요해요." };
  const title = input.title.trim();
  const description = input.description.trim();
  const errors = validateInfo(title, description);
  if (Object.keys(errors).length > 0) return { ok: false, message: "입력값을 확인해 주세요.", errors };
  if (!hasBackend) return { ok: false, message: NO_BACKEND };

  try {
    const { data } = await apiFetch<ApiResponse<DemoDetail>>("/api/v1/demos", {
      method: "POST",
      body: JSON.stringify({ title, description, tags: parseTags(input.tags) }),
    });
    return { ok: true, message: "초안을 만들었어요.", demoId: data.id };
  } catch (error) {
    if (error instanceof ApiRequestError) return { ok: false, message: error.error.message };
    throw error;
  }
}

export async function updateDemoInfo(demoId: number, previous: FormState, formData: FormData): Promise<FormState> {
  const values = readValues(formData, ["title", "description", "tags"]);
  const errors = validateInfo(values.title, values.description);
  const state = await runForm(previous, values, errors, () =>
    mutate(
      `/api/v1/demos/${demoId}`,
      "PUT",
      { title: values.title, description: values.description, tags: parseTags(values.tags) },
      "데모 정보를 수정했어요.",
      !hasBackend,
    ),
  );
  if (state.ok) revalidateDemo(demoId);
  return state;
}

export async function publishDemoVersion(demoId: number, versionId?: number): Promise<MutationResult> {
  const result = await mutate(
    `/api/v1/demos/${demoId}/publish`,
    "POST",
    versionId === undefined ? {} : { versionId },
    "데모를 공개했어요.",
    !hasBackend,
  );
  if (result.ok) revalidateDemo(demoId);
  return result;
}

export async function unpublishDemo(demoId: number): Promise<MutationResult> {
  const result = await mutate(`/api/v1/demos/${demoId}/unpublish`, "POST", undefined, "공개를 내렸어요.", !hasBackend);
  if (result.ok) revalidateDemo(demoId);
  return result;
}

export async function deleteOwnDemo(demoId: number): Promise<MutationResult> {
  const result = await mutate(`/api/v1/demos/${demoId}`, "DELETE", undefined, "데모를 삭제했어요.", !hasBackend);
  if (result.ok) revalidateDemo(demoId);
  return result;
}

/** 파일을 올린 뒤 화면에 새 버전이 보이도록 캐시를 비운다. */
export async function refreshDemo(demoId: number): Promise<void> {
  revalidateDemo(demoId);
}
