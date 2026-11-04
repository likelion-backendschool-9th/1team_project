"use server";

import { revalidatePath } from "next/cache";
import { apiFetch, ApiRequestError, isMockMode } from "@/app/_lib/api/client";

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

export async function toggleBookmark(feedId: number, bookmark: boolean) {
  if (!isId(feedId)) return INVALID_REQUEST;
  return request(`/api/v1/feeds/${feedId}/bookmark`, bookmark ? "POST" : "DELETE");
}

export async function reportFeed(feedId: number) {
  if (!isId(feedId)) return { ok: false, message: "잘못된 요청이에요." };
  const result = await request(`/api/v1/feeds/${feedId}/report`, "POST");
  return { ok: result.ok, message: result.message ?? "신고를 접수했어요. 운영진이 확인할게요." };
}

export async function blockUser(userId: number) {
  if (!isId(userId)) return { ok: false, message: "잘못된 요청이에요." };
  const result = await request(`/api/v1/blocks/${userId}`, "POST");
  return { ok: result.ok, message: result.message ?? "차단했어요. 이제 이 사용자의 글이 보이지 않아요." };
}

export async function deleteFeed(feedId: number) {
  if (!isId(feedId)) return { ok: false, message: "잘못된 요청이에요." };
  const result = await request(`/api/v1/feeds/${feedId}`, "DELETE");
  if (result.ok) {
    revalidatePath("/my/feeds");
    revalidatePath("/main");
    revalidatePath(`/feeds/${feedId}`);
  }
  return { ok: result.ok, message: result.message ?? "피드를 삭제했어요." };
}
