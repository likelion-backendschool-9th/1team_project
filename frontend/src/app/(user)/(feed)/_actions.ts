"use server";

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
