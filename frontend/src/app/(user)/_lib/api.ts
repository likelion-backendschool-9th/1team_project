import "server-only";
import {
  apiFetch,
  ApiRequestError,
  isMockMode,
  type ApiResponse,
  type PageResponse,
} from "@/app/_lib/api/client";
import type {
  Following,
  MyProfile,
  RankingEntry,
  RankingPeriod,
} from "@/app/_lib/types/user";
import type { AppNotification } from "@/app/_lib/types/notification";
import {
  createMockNotifications,
  mockFollowings,
  mockMe,
  mockRanking,
  mockUnreadCount,
} from "./mock";

export async function getMe(): Promise<MyProfile | null> {
  if (isMockMode) return mockMe;
  try {
    const { data } = await apiFetch<ApiResponse<MyProfile>>("/api/v1/users/me");
    return data;
  } catch (e) {
    if (e instanceof ApiRequestError && e.status === 401) return null;
    throw e;
  }
}

export async function getFollowings(): Promise<Following[]> {
  if (isMockMode) return mockFollowings;
  const [{ data }, presence] = await Promise.all([
    apiFetch<ApiResponse<PageResponse<Following>>>("/api/v1/follows/followings"),
    apiFetch<ApiResponse<{ userId: number; online: boolean }[]>>(
      "/api/v1/follows/followings/presence",
    ),
  ]);
  const online = new Set(presence.data.filter((item) => item.online).map((item) => item.userId));
  return data.content.map((user) => ({ ...user, online: online.has(user.id) }));
}

export async function getRanking(period: RankingPeriod): Promise<RankingEntry[]> {
  if (isMockMode) return mockRanking[period];
  const { data } = await apiFetch<ApiResponse<RankingEntry[]>>(
    `/api/v1/rankings?type=follower&period=${period}`,
  );
  return data;
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
