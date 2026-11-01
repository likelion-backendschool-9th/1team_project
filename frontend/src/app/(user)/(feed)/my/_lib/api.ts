import "server-only";
import { apiFetch, isMockMode, type ApiResponse, type PageResponse } from "@/app/_lib/api/client";
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
  mockSubscriptions,
  type BlockedUser,
  type Bookmark,
  type BookmarkType,
  type ChipHistory,
  type ChipPackage,
  type FollowUser,
  type MyAccount,
  type PlusMembership,
  type Settings,
  type Subscription,
} from "./mock";

async function get<T>(path: `/api/v1/${string}`, mock: T): Promise<T> {
  if (isMockMode) return mock;
  const { data } = await apiFetch<ApiResponse<T>>(path);
  return data;
}

async function getPage<T>(path: `/api/v1/${string}`, mock: T[]): Promise<T[]> {
  if (isMockMode) return mock;
  const { data } = await apiFetch<ApiResponse<PageResponse<T>>>(path);
  return data.content;
}

export const getAccount = () => get<MyAccount>("/api/v1/users/me", mockAccount);
export const getFollowings = () => getPage<FollowUser>("/api/v1/follows/followings", mockFollowings);
export const getFollowers = () => getPage<FollowUser>("/api/v1/follows/followers", mockFollowers);
export const getSettings = () => get<Settings>("/api/v1/users/me/settings", mockSettings);
export const getBlocked = async (): Promise<BlockedUser[]> => mockBlocked;
export const getPackages = async (): Promise<ChipPackage[]> => mockPackages;
export const getChipHistory = () => getPage<ChipHistory>("/api/v1/coins/history", mockHistory);
// 구독 목록 조회 API는 아직 정해지지 않았다 (프론트엔드-청사진 11장).
export const getSubscriptions = async (): Promise<Subscription[]> => mockSubscriptions;
// 딥소스 플러스 구독 조회 API는 아직 정해지지 않았다 (프론트엔드-청사진 11장).
export const getPlus = async (): Promise<PlusMembership> => mockPlus;

export async function getBookmarks(type: BookmarkType | undefined): Promise<Bookmark[]> {
  if (isMockMode) return mockBookmarks.filter((item) => !type || item.type === type);
  const query = type ? `?${new URLSearchParams({ type })}` : "";
  const { data } = await apiFetch<ApiResponse<PageResponse<Bookmark>>>(`/api/v1/users/me/bookmarks${query}`);
  return data.content;
}
