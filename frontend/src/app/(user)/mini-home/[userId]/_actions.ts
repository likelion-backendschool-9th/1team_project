"use server";

import { revalidatePath } from "next/cache";
import { redirect } from "next/navigation";
import {
  apiFetch,
  ApiRequestError,
  isMockMode,
  type ApiResponse,
} from "@/app/_lib/api/client";
import { runForm } from "@/app/_lib/api/formAction";
import { mutate } from "@/app/_lib/api/mutate";
import { readValues, type FormState } from "@/app/_lib/formState";
import { MAX_COMMENT_LENGTH, type CommentState } from "./_lib/commentState";
import { pickRandomMockUserId } from "./_lib/mock";

export type ActionResult = { ok: boolean; message?: string };

function isId(value: number) {
  return Number.isSafeInteger(value) && value > 0;
}

export async function toggleIlchon(userId: number, follow: boolean): Promise<ActionResult> {
  if (!isId(userId)) return { ok: false, message: "잘못된 요청이에요." };
  if (isMockMode) return { ok: true };

  try {
    await apiFetch(`/api/v1/follows/${userId}`, { method: follow ? "POST" : "DELETE" });
  } catch (error) {
    if (error instanceof ApiRequestError) return { ok: false, message: error.error.message };
    throw error;
  }
  return { ok: true };
}

export async function surf(currentUserId: number) {
  let nextId: number;
  if (isMockMode) {
    nextId = pickRandomMockUserId(isId(currentUserId) ? currentUserId : 0);
  } else {
    const { data } = await apiFetch<ApiResponse<{ userId: number }>>(
      "/api/v1/mini-homes/random",
    );
    nextId = data.userId;
  }
  redirect(`/mini-home/${nextId}`);
}

export async function writeComment(
  userId: number,
  previous: CommentState,
  formData: FormData,
): Promise<CommentState> {
  const content = String(formData.get("content") ?? "").trim();
  const attempt = previous.attempt + 1;
  const failed = (error: string): CommentState => ({
    values: { content },
    error,
    message: null,
    attempt,
  });

  if (!isId(userId)) return failed("잘못된 요청이에요.");
  if (!content) return failed("일촌평을 입력해 주세요.");
  if (content.length > MAX_COMMENT_LENGTH)
    return failed(`${MAX_COMMENT_LENGTH}자 이하로 입력해 주세요.`);

  if (isMockMode)
    return {
      values: { content: "" },
      error: null,
      message: "백엔드 연결 전이라 저장되지 않았어요.",
      attempt,
    };

  try {
    await apiFetch(`/api/v1/mini-homes/${userId}/comments`, {
      method: "POST",
      body: JSON.stringify({ content }),
    });
  } catch (error) {
    if (error instanceof ApiRequestError) return failed(error.error.message);
    throw error;
  }
  revalidatePath(`/mini-home/${userId}`);
  return { values: { content: "" }, error: null, message: "일촌평을 남겼어요.", attempt };
}

export async function writeGuestbook(
  userId: number,
  previous: FormState,
  formData: FormData,
): Promise<FormState> {
  const values = readValues(formData, ["content"]);
  const secret = formData.get("secret") === "on";
  const errors: Record<string, string> = {};
  if (!values.content) errors.content = "방명록 내용을 입력해 주세요.";
  else if (values.content.length > 300) errors.content = "300자 이하로 입력해 주세요.";

  const state = await runForm(previous, values, errors, () =>
    mutate(`/api/v1/mini-homes/${userId}/guestbook`, "POST", { ...values, secret }, "방명록을 남겼어요."),
  );
  if (state.ok) revalidatePath(`/mini-home/${userId}/guestbook`);
  return state.ok ? { ...state, values: {} } : state;
}

export async function deleteGuestbook(guestbookId: number) {
  if (!isId(guestbookId)) return { ok: false, message: "잘못된 요청이에요." };
  return mutate(`/api/v1/mini-homes/guestbook/${guestbookId}`, "DELETE", undefined, "방명록을 삭제했어요.");
}

export async function saveMiniHome(previous: FormState, formData: FormData): Promise<FormState> {
  const values = readValues(formData, ["skin", "headline", "domain", "bgm", "quote", "stackTags"]);
  const featuredDemoId = Number(formData.get("featuredDemoId"));
  const errors: Record<string, string> = {};
  if (!values.headline) errors.headline = "한 줄 소개를 입력해 주세요.";
  else if (values.headline.length > 30) errors.headline = "30자 이하로 입력해 주세요.";
  if (values.quote.length > 80) errors.quote = "80자 이하로 입력해 주세요.";

  const state = await runForm(previous, values, errors, async () => {
    const saved = await mutate("/api/v1/mini-homes/me", "PUT", values, "미니홈피를 저장했어요.");
    if (!saved.ok || !isId(featuredDemoId)) return saved;
    const featured = await mutate(
      "/api/v1/mini-homes/me/featured-demo",
      "PUT",
      { demoId: featuredDemoId },
      "미니홈피를 저장했어요.",
    );
    return featured.ok ? saved : featured;
  });
  return state;
}
