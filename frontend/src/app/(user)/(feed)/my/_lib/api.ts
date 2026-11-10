import "server-only";
import { apiFetch, hasBackend, isMockMode, type ApiResponse, type PageResponse } from "@/app/_lib/api/client";
import {
  mockAccount,
  mockBlocked,
  mockBookmarks,
  mockFollowers,
  mockFollowings,
  mockHistory,
  mockPackages,
  mockPlus,
  mockSettings,
  type BlockedUser,
  type Bookmark,
  type BookmarkType,
  type ChipHistory,
  type ChipPackage,
  type FollowUser,
  type MyAccount,
  type PlusMembership,
  type Settings,
} from "./mock";

async function getPage<T>(path: `/api/v1/${string}`, mock: T[]): Promise<T[]> {
  if (isMockMode) return mock;
  const { data } = await apiFetch<ApiResponse<PageResponse<T>>>(path);
  return data.content;
}

export async function getAccount(): Promise<MyAccount> {
  if (!hasBackend) return mockAccount;
  const { data } = await apiFetch<ApiResponse<{
    bio: string | null;
    email: string;
    githubUsername: string | null;
  }>>("/api/v1/users/me");
  return {
    bio: data.bio ?? "",
    githubUrl: data.githubUsername ? `https://github.com/${data.githubUsername}` : "",
    email: data.email,
    provider: mockAccount.provider,
    joinedAt: mockAccount.joinedAt,
  };
}

export const FOLLOW_PAGE_SIZE = 20;

export type FollowPage = { users: FollowUser[]; page: number; totalPages: number; total: number };

// 팔로우 API는 백엔드에 있어서, 백엔드가 연결돼 있으면 MOCK_EXCEPT_DM이어도 실서버를 쓴다.
// page는 화면 기준 1부터, 백엔드는 0부터 센다.
// q는 닉네임 검색어. 비어 있으면 전체.
async function getFollowPage(
  path: `/api/v1/${string}`,
  mock: FollowUser[],
  page: number,
  size: number,
  q: string,
): Promise<FollowPage> {
  if (!hasBackend) {
    const keyword = q.toLowerCase();
    const users = keyword ? mock.filter((user) => user.nickname.toLowerCase().includes(keyword)) : mock;
    return { users, page: 1, totalPages: 1, total: users.length };
  }
  const query = new URLSearchParams({ page: String(page - 1), size: String(size) });
  if (q) query.set("q", q);
  const { data } = await apiFetch<ApiResponse<PageResponse<FollowUser>>>(`${path}?${query}`);
  return { users: data.content, page, totalPages: data.totalPages, total: data.totalElements };
}

export const getFollowings = (page = 1, size = FOLLOW_PAGE_SIZE, q = "") =>
  getFollowPage("/api/v1/follows/followings", mockFollowings, page, size, q);
export const getFollowers = (page = 1, size = FOLLOW_PAGE_SIZE, q = "") =>
  getFollowPage("/api/v1/follows/followers", mockFollowers, page, size, q);
// 설정은 백엔드(GET users/me/settings)에 있어서, 백엔드가 연결돼 있으면 MOCK_EXCEPT_DM이어도 실서버를 쓴다.
export async function getSettings(): Promise<Settings> {
  if (!hasBackend) return mockSettings;
  const { data } = await apiFetch<ApiResponse<Settings>>("/api/v1/users/me/settings");
  return data;
}
export async function getBlocked(): Promise<BlockedUser[]> {
  if (!hasBackend) return mockBlocked;
  const { data } = await apiFetch<ApiResponse<BlockedUser[]>>("/api/v1/blocks");
  return data;
}
// 패키지 목록은 백엔드 API 없이 프론트에서 고정값 사용 (백엔드 PACKAGES 상수와 동기화)
export const getPackages = async (): Promise<ChipPackage[]> => mockPackages;

export const HISTORY_PAGE_SIZE = 20;

export type HistoryPage = {
  items: ChipHistory[];
  page: number;
  totalPages: number;
  total: number;
};

/** 거래 내역을 페이지 단위로 조회함. page는 1-based */
export async function getChipHistory(page = 1, size = HISTORY_PAGE_SIZE): Promise<HistoryPage> {
  if (!hasBackend) {
    // 목 모드에서도 페이지네이션이 실제처럼 동작하도록 slice 처리
    const total = mockHistory.length;
    const totalPages = Math.max(1, Math.ceil(total / size));
    const safePage = Math.min(Math.max(1, page), totalPages);
    const start = (safePage - 1) * size;
    const items = mockHistory.slice(start, start + size);
    return { items, page: safePage, totalPages, total };
  }
  const query = new URLSearchParams({ page: String(page - 1), size: String(size) });
  const { data } = await apiFetch<ApiResponse<PageResponse<ChipHistory>>>(`/api/v1/coins/history?${query}`);
  return { items: data.content, page, totalPages: data.totalPages, total: data.totalElements };
}
// 딥소스 플러스 구독 조회 API는 아직 정해지지 않았다 (프론트엔드-청사진 11장).
export const getPlus = async (): Promise<PlusMembership> => mockPlus;

export async function getBookmarks(type: BookmarkType | undefined): Promise<Bookmark[]> {
  if (isMockMode) return mockBookmarks.filter((item) => !type || item.type === type);
  const query = type ? `?${new URLSearchParams({ type })}` : "";
  const { data } = await apiFetch<ApiResponse<PageResponse<Bookmark>>>(`/api/v1/users/me/bookmarks${query}`);
  return data.content;
}
