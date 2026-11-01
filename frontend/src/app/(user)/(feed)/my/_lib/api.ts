import "server-only";
import { apiFetch, isMockMode, type ApiResponse, type PageResponse } from "@/app/_lib/api/client";
import {
  mockAccount,
  mockBlocked,
  mockFollowers,
  mockFollowings,
  mockHistory,
  mockPackages,
  mockSettings,
  mockSubscriptions,
  type BlockedUser,
  type ChipHistory,
  type ChipPackage,
  type FollowUser,
  type MyAccount,
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
