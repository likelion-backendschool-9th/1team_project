import "server-only";
import { cache } from "react";
import {
  ApiRequestError,
  apiFetch,
  isMockMode,
  type ApiResponse,
  type PageResponse,
} from "@/app/_lib/api/client";
import type {
  GithubContributions,
  GithubLanguage,
  GithubRepo,
  MiniHomeComment,
  MiniHomeDemo,
  MiniHomePost,
  MiniHomeProfile,
  VisitorStats,
} from "@/app/_lib/types/miniHome";

type MiniHomeSettings = {
  headline: string;
  quote: string | null;
  skin: MiniHomeProfile["skin"];
  domain: string | null;
  bgm: string | null;
  showcaseDemoIds: number[];
  featuredDemoId: number | null;
  stackTags: string[];
  featuredPostId: number | null;
};
import {
  createMockComments,
  findMockDemos,
  findMockProfile,
  createMockDemoComments,
  createMockGuestbook,
  mockContributions,
  mockLanguages,
  mockPosts,
  mockRepos,
  mockVisitors,
} from "./mock";

export async function getServerTime(): Promise<number> {
  return Date.now();
}

// 레이아웃과 페이지가 같은 요청 안에서 함께 쓰므로 cache로 한 번만 조회한다.
async function loadOrNull<T>(path: `/api/v1/${string}`): Promise<T | null> {
  try {
    const { data } = await apiFetch<ApiResponse<T>>(path);
    return data;
  } catch (error) {
    if (error instanceof ApiRequestError && error.status === 404) return null;
    throw error;
  }
}

export const getProfile = cache(async (userId: number): Promise<MiniHomeProfile | null> => {
  if (isMockMode) return findMockProfile(userId);
  const [user, home] = await Promise.all([
    loadOrNull<MiniHomeProfile>(`/api/v1/users/${userId}`),
    loadOrNull<MiniHomeSettings>(`/api/v1/mini-homes/${userId}`),
  ]);
  if (!user || !home) return null;
  return {
    ...user,
    headline: home.headline,
    quote: home.quote,
    skin: home.skin,
    domain: home.domain,
    bgm: home.bgm,
    showcaseDemoIds: home.showcaseDemoIds,
    featuredDemoId: home.featuredDemoId,
    stackTags: home.stackTags ?? [],
    featuredPostId: home.featuredPostId ?? null,
  };
});

export async function getVisitors(userId: number): Promise<VisitorStats> {
  if (isMockMode) return mockVisitors;
  const { data } = await apiFetch<ApiResponse<VisitorStats>>(
    `/api/v1/mini-homes/${userId}/visitors`,
  );
  return data;
}

export async function getLanguages(userId: number): Promise<GithubLanguage[]> {
  if (isMockMode) return mockLanguages;
  const { data } = await apiFetch<ApiResponse<GithubLanguage[]>>(
    `/api/v1/users/${userId}/github/languages`,
  );
  return data;
}

export async function getContributions(userId: number): Promise<GithubContributions> {
  if (isMockMode) return mockContributions;
  const { data } = await apiFetch<ApiResponse<GithubContributions>>(
    `/api/v1/users/${userId}/github/contributions`,
  );
  return data;
}

export async function getRepos(userId: number): Promise<GithubRepo[]> {
  if (isMockMode) return mockRepos;
  const { data } = await apiFetch<ApiResponse<GithubRepo[]>>(
    `/api/v1/users/${userId}/github/repos`,
  );
  return data;
}

export async function getDemos(userId: number, size = 3): Promise<MiniHomeDemo[]> {
  if (isMockMode) return findMockDemos(userId).slice(0, size);
  const { data } = await apiFetch<ApiResponse<PageResponse<MiniHomeDemo>>>(
    `/api/v1/demos?userId=${userId}&size=${size}`,
  );
  return data.content;
}

export async function getRecentPosts(userId: number): Promise<MiniHomePost[]> {
  if (isMockMode) return mockPosts;
  const { data } = await apiFetch<ApiResponse<PageResponse<MiniHomePost>>>(
    `/api/v1/users/${userId}/posts?size=3`,
  );
  return data.content;
}

export async function getComments(
  userId: number,
): Promise<{ items: MiniHomeComment[]; total: number }> {
  if (isMockMode) return createMockComments(Date.now());
  const { data } = await apiFetch<ApiResponse<PageResponse<MiniHomeComment>>>(
    `/api/v1/mini-homes/${userId}/comments?size=3`,
  );
  return { items: data.content, total: data.totalElements };
}

export async function getGuestbook(userId: number): Promise<MiniHomeComment[]> {
  if (isMockMode) return createMockGuestbook(Date.now());
  const { data } = await apiFetch<ApiResponse<PageResponse<MiniHomeComment>>>(
    `/api/v1/mini-homes/${userId}/guestbook`,
  );
  return data.content;
}

export async function getDemoComments(demoId: number): Promise<MiniHomeComment[]> {
  if (isMockMode) return createMockDemoComments(demoId, Date.now());
  const { data } = await apiFetch<ApiResponse<PageResponse<MiniHomeComment>>>(
    `/api/v1/demos/${demoId}/comments`,
  );
  return data.content;
}
