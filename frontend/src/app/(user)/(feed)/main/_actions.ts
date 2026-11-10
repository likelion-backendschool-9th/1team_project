"use server";

import { cookies } from "next/headers";
import { revalidatePath } from "next/cache";
import { apiFetch, ApiRequestError, isMockMode } from "@/app/_lib/api/client";
import { MAX_FEED_LENGTH, type ComposerState } from "./_lib/composerState";

const API_ORIGIN = process.env.API_ORIGIN;

export type UploadedImage = { url: string; thumbnailUrl: string };

/** 이미지를 R2에 업로드하고 { url, thumbnailUrl } 목록을 반환한다. */
export async function uploadImages(
  formData: FormData,
): Promise<{ images: UploadedImage[] } | { error: string }> {
  if (!API_ORIGIN) return { error: "백엔드 연결 전이에요." };

  const cookieStr = (await cookies()).toString();
  try {
    const res = await fetch(`${API_ORIGIN}/api/v1/images`, {
      method: "POST",
      // Content-Type을 직접 지정하지 않아야 브라우저가 multipart boundary를 자동으로 설정한다.
      headers: { cookie: cookieStr },
      body: formData,
      cache: "no-store",
    });
    if (!res.ok) {
      const body = await res.json().catch(() => null);
      const msg =
        body && typeof body === "object" && "error" in body
          ? (body as { error: { message: string } }).error.message
          : "이미지 업로드에 실패했어요.";
      return { error: msg };
    }
    const data = (await res.json()) as { data: UploadedImage[] };
    return { images: data.data };
  } catch {
    return { error: "이미지 업로드 중 오류가 발생했어요." };
  }
}

export async function createFeed(
  previous: ComposerState,
  formData: FormData,
): Promise<ComposerState> {
  const content = String(formData.get("content") ?? "").trim();
  const rawDemoId = String(formData.get("demoId") ?? "");
  const demoId = Number(rawDemoId);
  const linkUrl = String(formData.get("linkUrl") ?? "").trim();
  const imageUrls = (formData.getAll("imageUrls") as string[]).filter(Boolean);
  const attempt = previous.attempt + 1;

  const failed = (error: string): ComposerState => ({
    values: { content, demoId: rawDemoId, linkUrl },
    error,
    message: null,
    attempt,
  });

  if (!content) return failed("내용을 입력해 주세요.");
  if (rawDemoId && !(Number.isSafeInteger(demoId) && demoId > 0))
    return failed("첨부할 데모를 다시 골라 주세요.");
  if (content.length > MAX_FEED_LENGTH)
    return failed(`${MAX_FEED_LENGTH}자 이하로 입력해 주세요.`);
  if (linkUrl && !/^https?:\/\/.+/.test(linkUrl))
    return failed("링크는 http:// 또는 https://로 시작해야 합니다.");

  if (isMockMode)
    return {
      values: { content: "", demoId: "", linkUrl: "" },
      error: null,
      message: rawDemoId
        ? "데모를 첨부해 게시했어요. (백엔드 연결 전이라 저장되지 않았어요)"
        : "백엔드 연결 전이라 저장되지 않았어요.",
      attempt,
    };

  try {
    await apiFetch("/api/v1/feeds", {
      method: "POST",
      body: JSON.stringify({
        content,
        demoId: rawDemoId ? demoId : null,
        imageUrls: imageUrls.length > 0 ? imageUrls : null,
        linkUrl: linkUrl || null,
      }),
    });
  } catch (error) {
    if (error instanceof ApiRequestError) return failed(error.error.message);
    throw error;
  }
  revalidatePath("/");
  revalidatePath("/main");
  return {
    values: { content: "", demoId: "", linkUrl: "" },
    error: null,
    message: "피드를 게시했어요.",
    attempt,
  };
}
