"use server";

import { getFollowers, getFollowings, type FollowPage } from "../_lib/api";
import { parseFollowQuery, parseFollowTab } from "./_lib/params";

/**
 * 무한 스크롤의 다음 페이지. 목록은 스크롤할 때마다 클라이언트에서 이어 받아서 이 Action으로 조회한다.
 * 로그인 여부는 백엔드가 JWT 쿠키로 확인한다(비로그인이면 401).
 */
export async function loadFollowPage(tab: unknown, page: unknown, q: unknown): Promise<FollowPage> {
  const safeTab = parseFollowTab(tab);
  const safePage = typeof page === "number" && Number.isSafeInteger(page) && page > 0 ? page : 1;
  const keyword = parseFollowQuery(q);
  return safeTab === "followers"
    ? getFollowers(safePage, undefined, keyword)
    : getFollowings(safePage, undefined, keyword);
}
