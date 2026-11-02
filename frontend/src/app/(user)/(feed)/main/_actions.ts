"use server";

import { revalidatePath } from "next/cache";
import { apiFetch, ApiRequestError, isMockMode } from "@/app/_lib/api/client";
import { MAX_FEED_LENGTH, type ComposerState } from "./_lib/composerState";

export async function createFeed(
  previous: ComposerState,
  formData: FormData,
): Promise<ComposerState> {
  const content = String(formData.get("content") ?? "").trim();
  const rawDemoId = String(formData.get("demoId") ?? "");
  const demoId = Number(rawDemoId);
  const attempt = previous.attempt + 1;
  const failed = (error: string): ComposerState => ({
    values: { content, demoId: rawDemoId },
    error,
    message: null,
    attempt,
  });

  if (!content) return failed("내용을 입력해 주세요.");
  if (rawDemoId && !(Number.isSafeInteger(demoId) && demoId > 0)) return failed("첨부할 데모를 다시 골라 주세요.");
  if (content.length > MAX_FEED_LENGTH)
    return failed(`${MAX_FEED_LENGTH}자 이하로 입력해 주세요.`);

  if (isMockMode)
    return {
      values: { content: "", demoId: "" },
      error: null,
      message: rawDemoId
        ? "데모를 첨부해 게시했어요. (백엔드 연결 전이라 저장되지 않았어요)"
        : "백엔드 연결 전이라 저장되지 않았어요.",
      attempt,
    };

  try {
    await apiFetch("/api/v1/feeds", {
      method: "POST",
      body: JSON.stringify({ content, demoId: rawDemoId ? demoId : null }),
    });
  } catch (error) {
    if (error instanceof ApiRequestError) return failed(error.error.message);
    throw error;
  }
  revalidatePath("/main");
  return {
    values: { content: "", demoId: "" },
    error: null,
    message: "피드를 게시했어요.",
    attempt,
  };
}
