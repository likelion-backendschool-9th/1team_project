"use server";

import { revalidatePath } from "next/cache";
import { apiFetch, ApiRequestError, isMockMode } from "@/app/_lib/api/client";
import { MAX_FEED_LENGTH, type ComposerState } from "./_lib/composerState";

export type ActionResult = { ok: boolean; message?: string };

const INVALID_REQUEST: ActionResult = { ok: false, message: "잘못된 요청이에요." };

function isId(value: number) {
  return Number.isSafeInteger(value) && value > 0;
}

async function request(
  path: `/api/v1/${string}`,
  method: "POST" | "DELETE",
): Promise<ActionResult> {
  if (isMockMode) return { ok: true };

  try {
    await apiFetch(path, { method });
  } catch (error) {
    if (error instanceof ApiRequestError)
      return { ok: false, message: error.error.message };
    throw error;
  }
  return { ok: true };
}

export async function createFeed(
  previous: ComposerState,
  formData: FormData,
): Promise<ComposerState> {
  const content = String(formData.get("content") ?? "").trim();
  const attempt = previous.attempt + 1;
  const failed = (error: string): ComposerState => ({
    values: { content },
    error,
    message: null,
    attempt,
  });

  if (!content) return failed("내용을 입력해 주세요.");
  if (content.length > MAX_FEED_LENGTH)
    return failed(`${MAX_FEED_LENGTH}자 이하로 입력해 주세요.`);

  if (isMockMode)
    return {
      values: { content: "" },
      error: null,
      message: "백엔드 연결 전이라 저장되지 않았어요.",
      attempt,
    };

  try {
    await apiFetch("/api/v1/feeds", {
      method: "POST",
      body: JSON.stringify({ content }),
    });
  } catch (error) {
    if (error instanceof ApiRequestError) return failed(error.error.message);
    throw error;
  }
  revalidatePath("/main");
  return {
    values: { content: "" },
    error: null,
    message: "피드를 게시했어요.",
    attempt,
  };
}

export async function toggleLike(feedId: number, like: boolean) {
  if (!isId(feedId)) return INVALID_REQUEST;
  return request(`/api/v1/feeds/${feedId}/like`, like ? "POST" : "DELETE");
}

export async function toggleRepost(feedId: number, repost: boolean) {
  if (!isId(feedId)) return INVALID_REQUEST;
  return request(`/api/v1/feeds/${feedId}/repost`, repost ? "POST" : "DELETE");
}

export async function toggleFollow(userId: number, follow: boolean) {
  if (!isId(userId)) return INVALID_REQUEST;
  return request(`/api/v1/follows/${userId}`, follow ? "POST" : "DELETE");
}
