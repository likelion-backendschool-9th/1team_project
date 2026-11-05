export type FollowTab = "followings" | "followers";

/** 닉네임 검색어 최대 길이 (백엔드 닉네임 컬럼 길이와 같다) */
export const MAX_FOLLOW_QUERY = 30;

export function parseFollowTab(value: unknown): FollowTab {
  return value === "followers" ? "followers" : "followings";
}

export function parseFollowQuery(value: unknown): string {
  return typeof value === "string" ? value.trim().slice(0, MAX_FOLLOW_QUERY) : "";
}
