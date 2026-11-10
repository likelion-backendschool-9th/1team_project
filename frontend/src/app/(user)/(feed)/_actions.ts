'use server';

import { revalidatePath } from 'next/cache';
import {
  apiFetch,
  ApiRequestError,
  hasBackend,
  isMockMode,
} from '@/app/_lib/api/client';

export type ActionResult = { ok: boolean; message?: string };

const INVALID_REQUEST: ActionResult = {
  ok: false,
  message: '잘못된 요청이에요.',
};

function isId(value: number) {
  return Number.isSafeInteger(value) && value > 0;
}

async function request(
  path: `/api/v1/${string}`,
  method: 'POST' | 'DELETE',
  useMock = isMockMode,
): Promise<ActionResult> {
  if (useMock) return { ok: true };

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
  return request(`/api/v1/feeds/${feedId}/like`, like ? 'POST' : 'DELETE');
}

export async function toggleRepost(feedId: number, repost: boolean) {
  if (!isId(feedId)) return INVALID_REQUEST;
  return request(`/api/v1/feeds/${feedId}/repost`, repost ? 'POST' : 'DELETE');
}

export async function toggleFollow(userId: number, follow: boolean) {
  if (!isId(userId)) return INVALID_REQUEST;
  // 팔로우 API는 백엔드에 있어서, 백엔드가 연결돼 있으면 MOCK_EXCEPT_DM이어도 실서버를 쓴다.
  return request(
    `/api/v1/follows/${userId}`,
    follow ? 'POST' : 'DELETE',
    !hasBackend,
  );
}

export async function toggleBookmark(feedId: number, bookmark: boolean) {
  if (!isId(feedId)) return INVALID_REQUEST;
  return request(
    `/api/v1/feeds/${feedId}/bookmark`,
    bookmark ? 'POST' : 'DELETE',
  );
}

export async function reportFeed(feedId: number, reason: string) {
  if (!isId(feedId)) return { ok: false, message: '잘못된 요청이에요.' };
  if (isMockMode) return { ok: true, message: '신고가 접수되었습니다.' };
  try {
    await apiFetch(`/api/v1/feeds/${feedId}/report`, {
      method: 'POST',
      body: JSON.stringify({ reason }),
    });
  } catch (error) {
    if (error instanceof ApiRequestError)
      return { ok: false, message: error.error.message };
    throw error;
  }
  return { ok: true, message: '신고가 접수되었습니다.' };
}

export async function blockUser(userId: number) {
  if (!isId(userId)) return { ok: false, message: "잘못된 요청이에요." };
  const result = await request(`/api/v1/blocks/${userId}`, "POST", !hasBackend);
  return { ok: result.ok, message: result.message ?? "차단했어요. 이제 이 사용자의 글이 보이지 않고, 블로그 글 북마크도 해제했어요." };
}

export async function deleteFeed(feedId: number) {
  if (!isId(feedId)) return { ok: false, message: '잘못된 요청이에요.' };
  const result = await request(`/api/v1/feeds/${feedId}`, 'DELETE');
  if (result.ok) {
    revalidatePath('/my/feeds');
    revalidatePath('/main');
    revalidatePath(`/feeds/${feedId}`);
  }
  return { ok: result.ok, message: result.message ?? '피드를 삭제했어요.' };
}
