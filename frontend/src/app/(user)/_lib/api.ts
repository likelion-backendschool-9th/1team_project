import "server-only";
import {
  apiFetch,
  ApiRequestError,
  hasBackend,
  isMockMode,
  type ApiResponse,
  type PageResponse,
} from "@/app/_lib/api/client";
import type {
  Following,
  MyProfile,
} from "@/app/_lib/types/user";
import type { AppNotification } from "@/app/_lib/types/notification";
import {
  createMockNotifications,
  mockFollowings,
  mockMe,
  mockUnreadCount,
} from "./mock";

export async function getMe(): Promise<MyProfile | null> {
  if (!hasBackend) return mockMe;
  try {
    const { data } = await apiFetch<ApiResponse<MyProfile>>("/api/v1/users/me");
    // TEMP-DM-PREVIEW: 목 데이터와 섞어 쓸 때는 백엔드 응답에 없는 필드(뱃지·팔로워 수 등)를 목업으로 채운다.
    return isMockMode ? { ...mockMe, ...data } : data;
  } catch (e) {
    if (e instanceof ApiRequestError && e.status === 401) return null;
    throw e;
  }
}

export async function getFollowings(): Promise<Following[]> {
  // 팔로우 API는 백엔드에 있어서, 백엔드가 연결돼 있으면 MOCK_EXCEPT_DM이어도 실서버를 쓴다.
  if (!hasBackend) return mockFollowings;
  const { data } = await apiFetch<ApiResponse<PageResponse<Following>>>("/api/v1/follows/followings");
  if (data.content.length === 0) return [];
  // 접속 상태는 지금 받은 사람들의 아이디만 물어본다.
  const userIds = data.content.map((user) => user.id).join(",");
  const presence = await apiFetch<ApiResponse<{ userId: number; online: boolean }[]>>(
    `/api/v1/follows/followings/presence?userIds=${userIds}`,
  );
  const online = new Set(presence.data.filter((item) => item.online).map((item) => item.userId));
  return data.content.map((user) => ({ ...user, online: online.has(user.id) }));
}

export async function getUnreadCount(): Promise<number> {
  if (isMockMode) return mockUnreadCount;
  const { data } = await apiFetch<ApiResponse<{ count: number }>>(
    "/api/v1/notifications/unread-count",
  );
  return data.count;
}

export async function getServerTime(): Promise<number> {
  return Date.now();
}

export async function getNotifications(): Promise<AppNotification[]> {
  if (isMockMode) return createMockNotifications(Date.now());
  const { data } = await apiFetch<ApiResponse<PageResponse<AppNotification>>>(
    "/api/v1/notifications?size=20",
  );
  return data.content;
}
