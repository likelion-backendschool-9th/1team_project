export type FollowTab = "followings" | "followers";

/** 닉네임 검색어 최대 길이 (백엔드 닉네임 컬럼 길이와 같다) */
export const MAX_FOLLOW_QUERY = 30;

export function parseFollowTab(value: unknown): FollowTab {
  return value === "followers" ? "followers" : "followings";
}

export function parseFollowQuery(value: unknown): string {
  return typeof value === "string" ? value.trim().slice(0, MAX_FOLLOW_QUERY) : "";
}

/** 탭·검색어를 담은 팔로우 목록 주소. 탭을 바꿔도 검색어가 유지되게 같이 쓴다. */
export function followsHref(tab: FollowTab, q: string): string {
  const params = new URLSearchParams();
  if (tab === "followers") params.set("tab", "followers");
  if (q) params.set("q", q);
  const query = params.toString();
  return query ? `/my/follows?${query}` : "/my/follows";
}
