import "server-only";
import {
  apiFetch,
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
import {
  mockFollowings,
  mockMe,
  mockRanking,
  mockUnreadCount,
} from "./mock";

export async function getMe(): Promise<MyProfile> {
  if (isMockMode) return mockMe;
  const { data } = await apiFetch<ApiResponse<MyProfile>>("/api/v1/users/me");
  return data;
}

export async function getFollowings(): Promise<Following[]> {
  if (isMockMode) return mockFollowings;
  const { data } = await apiFetch<ApiResponse<PageResponse<Following>>>(
    "/api/v1/follows/followings",
  );
  return data.content;
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
