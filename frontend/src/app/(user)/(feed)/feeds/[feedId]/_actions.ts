"use server";

import { revalidatePath } from "next/cache";
import { runForm } from "@/app/_lib/api/formAction";
import { mutate } from "@/app/_lib/api/mutate";
import { readValues, type FormState } from "@/app/_lib/formState";

export async function createComment(
  feedId: number,
  previous: FormState,
  formData: FormData,
): Promise<FormState> {
  const values = readValues(formData, ["content"]);
  const errors: Record<string, string> = {};
  if (!values.content) errors.content = "댓글을 입력해 주세요.";
  else if (values.content.length > 300) errors.content = "300자 이하로 입력해 주세요.";

  const state = await runForm(previous, values, errors, () =>
    mutate(`/api/v1/feeds/${feedId}/comments`, "POST", values, "댓글을 남겼어요."),
  );
  if (state.ok) revalidatePath(`/feeds/${feedId}`);
  return state.ok ? { ...state, values: {} } : state;
}

export async function updateFeed(feedId: number, previous: FormState, formData: FormData): Promise<FormState> {
  const values = readValues(formData, ["content"]);
  const errors: Record<string, string> = {};
  if (!values.content) errors.content = "내용을 입력해 주세요.";
  else if (values.content.length > 500) errors.content = "500자 이하로 입력해 주세요.";
  const state = await runForm(previous, values, errors, () =>
    mutate(`/api/v1/feeds/${feedId}`, "PUT", values, "피드를 수정했어요."),
  );
  if (state.ok) {
    revalidatePath(`/feeds/${feedId}`);
    revalidatePath("/my/feeds");
    revalidatePath("/main");
  }
  return state;
}

export async function updateFeedComment(commentId: number, content: string) {
  const text = content.trim();
  if (!text || text.length > 300) return { ok: false, message: "1~300자로 입력해 주세요." };
  return mutate(`/api/v1/feeds/comments/${commentId}`, "PUT", { content: text }, "댓글을 수정했어요.");
}

export async function deleteFeedComment(commentId: number) {
  return mutate(`/api/v1/feeds/comments/${commentId}`, "DELETE", undefined, "댓글을 삭제했어요.");
}

export async function replyFeedComment(feedId: number, parentId: number, content: string) {
  const text = content.trim();
  if (!text || text.length > 300) return { ok: false, message: "1~300자로 입력해 주세요." };
  return mutate(`/api/v1/feeds/${feedId}/comments`, "POST", { content: text, parentId }, "답글을 남겼어요.");
}

export async function toggleFeedCommentLike(commentId: number, like: boolean) {
  return mutate(`/api/v1/feeds/comments/${commentId}/like`, like ? "POST" : "DELETE", undefined, "좋아요를 반영했어요.");
}
