import type { UserSummary } from "./user";

export type DmRoom = {
  user: UserSummary;
  online: boolean;
  lastMessage: string;
  lastAt: string;
  unreadCount: number;
};

export type DmMessage = {
  id: number;
  senderId: number;
  content: string;
  createdAt: string;
  read: boolean;
};

/** DM 창 사람 찾기에서 보여 주는 나와의 관계. null은 팔로우 관계가 없는 검색 결과다. */
export type DmCandidateRelation = "mutual" | "following" | "follower" | null;

export type DmCandidate = UserSummary & { relation: DmCandidateRelation };

/**
 * DM 창 사람 찾기의 다음 조회 위치. 팔로잉 → 팔로워 → 전체 회원 검색 순으로 넘어간다.
 * page는 1부터 센다. searchId는 회원 검색 페이지를 넘기는 동안 순서를 유지하는 값이다.
 */
export type DmCandidateCursor = {
  stage: "followings" | "followers" | "search";
  page: number;
  searchId?: string;
};

export type DmCandidatePage = { users: DmCandidate[]; next: DmCandidateCursor | null };
