import "server-only";
import { redirect } from "next/navigation";
import {
  apiFetch,
  ApiRequestError,
  hasBackend,
  isMockMode,
  type ApiResponse,
  type PageResponse,
} from "@/app/_lib/api/client";
import type {
  DmCandidate,
  DmCandidateCursor,
  DmCandidatePage,
  DmCandidateRelation,
  DmMessage,
  DmRoom,
} from "@/app/_lib/types/dm";
import type { UserSummary } from "@/app/_lib/types/user";
import { createMockMessages, createMockRooms, mockDmFollowers, mockDmFollowings } from "./dmMock";
import { mockUsers } from "./mock";

// DM은 로그인한 회원만 쓴다. 로그인이 풀렸으면(401) 로그인 화면으로 보낸다.
async function fetchOrLogin<T>(path: `/api/v1/${string}`): Promise<T> {
  try {
    return await apiFetch<T>(path);
  } catch (error) {
    if (error instanceof ApiRequestError && error.status === 401)
      redirect("/login");
    throw error;
  }
}

export async function getRooms(): Promise<DmRoom[]> {
  if (!hasBackend) return createMockRooms(Date.now());
  const { data } = await fetchOrLogin<ApiResponse<DmRoom[]>>("/api/v1/dm/rooms");
  return data;
}

export async function getMessages(myId: number, partnerId: number): Promise<DmMessage[]> {
  if (!hasBackend) return createMockMessages(myId, partnerId, Date.now());
  const { data } = await fetchOrLogin<ApiResponse<PageResponse<DmMessage>>>(
    `/api/v1/dm/${partnerId}`,
  );
  return data.content;
}

const CANDIDATE_PAGE_SIZE = 20;

type FollowUserResponse = UserSummary & { following: boolean; followsMe: boolean };
type UserSearchResponse = { id: number; nickname: string; profileImage?: string | null; techStacks?: string[] };
type StagePage = { users: DmCandidate[]; hasMore: boolean; searchId?: string };

function relationOf(user: FollowUserResponse): DmCandidateRelation {
  if (user.following && user.followsMe) return "mutual";
  return user.following ? "following" : "follower";
}

function matchesNickname(user: UserSummary, q: string) {
  return user.nickname.toLowerCase().includes(q.toLowerCase());
}

// 팔로우 API는 백엔드에 있어서, 백엔드가 연결돼 있으면 MOCK_EXCEPT_DM이어도 실서버를 쓴다.
async function getFollowStage(stage: "followings" | "followers", q: string, page: number): Promise<StagePage> {
  if (!hasBackend) {
    const mock = stage === "followings" ? mockDmFollowings : mockDmFollowers;
    return { users: mock.filter((user) => matchesNickname(user, q)), hasMore: false };
  }
  const query = new URLSearchParams({ page: String(page - 1), size: String(CANDIDATE_PAGE_SIZE) });
  if (q) query.set("q", q);
  const { data } = await fetchOrLogin<ApiResponse<PageResponse<FollowUserResponse>>>(`/api/v1/follows/${stage}?${query}`);
  return {
    users: data.content.map((user) => ({
      id: user.id,
      nickname: user.nickname,
      profileImage: user.profileImage,
      techStacks: user.techStacks,
      relation: relationOf(user),
    })),
    hasMore: page < data.totalPages,
  };
}

// 통합 검색의 회원 탭과 같은 API다. 검색어가 너무 짧은 등 입력 오류(400)면 더 찾지 않는다.
async function getSearchStage(q: string, page: number, searchId?: string): Promise<StagePage> {
  if (!q) return { users: [], hasMore: false };
  if (isMockMode) {
    return {
      users: Object.values(mockUsers)
        .filter((user) => matchesNickname(user, q))
        .map((user) => ({ ...user, relation: null })),
      hasMore: false,
    };
  }
  const query = new URLSearchParams({ q, type: "user", page: String(page - 1), size: String(CANDIDATE_PAGE_SIZE) });
  if (searchId) query.set("searchId", searchId);
  try {
    const { data } = await fetchOrLogin<ApiResponse<PageResponse<UserSearchResponse> & { searchId?: string }>>(
      `/api/v1/search?${query}`,
    );
    return {
      users: data.content.map((user) => ({
        id: user.id,
        nickname: user.nickname,
        profileImage: user.profileImage ?? null,
        techStacks: user.techStacks ?? [],
        relation: null,
      })),
      hasMore: page < data.totalPages,
      searchId: data.searchId,
    };
  } catch (error) {
    if (error instanceof ApiRequestError && error.status === 400) return { users: [], hasMore: false };
    throw error;
  }
}

function nextStage(stage: DmCandidateCursor["stage"], q: string): DmCandidateCursor | null {
  if (stage === "followings") return { stage: "followers", page: 1 };
  // 검색어 없이 열었을 때는 팔로우한·팔로우하는 사람까지만 보여 준다.
  if (stage === "followers" && q) return { stage: "search", page: 1 };
  return null;
}

/**
 * DM 보낼 사람 후보 한 페이지. 팔로잉 → 팔로워 → 전체 회원 검색 순으로 이어 준다.
 * 단계 사이의 중복(맞팔은 두 목록에 다 있음)은 화면에서 거른다.
 * 한 단계가 비어 있으면 다음 단계까지 이어 받아서, 다음이 있는데 빈 페이지를 돌려주지 않는다.
 */
export async function getDmCandidates(q: string, cursor: DmCandidateCursor): Promise<DmCandidatePage> {
  let current = cursor;
  for (;;) {
    const { stage, page, searchId } = current;
    const result: StagePage =
      stage === "search" ? await getSearchStage(q, page, searchId) : await getFollowStage(stage, q, page);
    const next = result.hasMore ? { stage, page: page + 1, searchId: result.searchId } : nextStage(stage, q);
    if (result.users.length > 0 || !next) return { users: result.users, next };
    current = next;
  }
}
