import "server-only";
import { cache } from "react";
import {
  ApiRequestError,
  apiFetch,
  hasBackend,
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
    // 홈피에서 따로 정하지 않았으면 온보딩에서 고른 기술 스택을 보여 준다.
    stackTags: home.stackTags?.length ? home.stackTags : (user.techStacks ?? []),
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
  if (!hasBackend) return findMockDemos(userId).slice(0, size);
  const { data } = await apiFetch<ApiResponse<{ content: MiniHomeDemo[] }>>(
    `/api/v1/demos?userId=${userId}&size=${size}`,
  );
  return data.content;
}

type PostRow = { id: number; title: string; price: number; paid?: boolean; seriesId?: number | null };

function toMiniHomePost(post: PostRow): MiniHomePost {
  return {
    id: post.id,
    title: post.title,
    paid: post.paid ?? post.price > 0,
    price: post.price,
    seriesId: post.seriesId ?? null,
  };
}

export async function getRecentPosts(userId: number): Promise<MiniHomePost[]> {
  if (isMockMode) return mockPosts;
  const { data } = await apiFetch<ApiResponse<PageResponse<PostRow>>>(`/api/v1/users/${userId}/posts?size=3`);
  return data.content.map(toMiniHomePost);
}

// 홈 카드에 보여 줄 최근 방명록 3개와 전체 수. (일촌평은 방명록으로 통일했다)
export async function getRecentGuestbook(
  userId: number,
): Promise<{ items: MiniHomeComment[]; total: number }> {
  if (isMockMode) {
    const items = createMockGuestbook(Date.now());
    return { items: items.slice(0, 3), total: items.length };
  }
  const { data } = await apiFetch<ApiResponse<PageResponse<MiniHomeComment>>>(
    `/api/v1/mini-homes/${userId}/guestbook?size=3`,
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

// 미니홈피 수정 화면의 "제작기 글" 선택칸과 블로그 탭에 쓴다.
export async function getMyPosts(userId: number, size = 20): Promise<MiniHomePost[]> {
  if (isMockMode) return mockPosts;
  const { data } = await apiFetch<ApiResponse<PageResponse<PostRow>>>(`/api/v1/users/${userId}/posts?size=${size}`);
  return data.content.map(toMiniHomePost);
}

export async function getDemoComments(demoId: number): Promise<MiniHomeComment[]> {
  if (isMockMode) return createMockDemoComments(demoId, Date.now());
  const { data } = await apiFetch<ApiResponse<PageResponse<MiniHomeComment>>>(
    `/api/v1/demos/${demoId}/comments`,
  );
  return data.content;
}
