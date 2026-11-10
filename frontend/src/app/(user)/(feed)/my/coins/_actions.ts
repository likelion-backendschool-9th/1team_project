"use server";

import { getChipHistory, type HistoryPage } from "../_lib/api";

/**
 * 무한 스크롤의 다음 페이지.
 * 클라이언트가 스크롤 끝에 닿을 때마다 호출함.
 * 로그인 여부는 백엔드가 JWT 쿠키로 확인함 (비로그인이면 401)
 */
export async function loadHistoryPage(page: unknown): Promise<HistoryPage> {
  const safePage =
    typeof page === "number" && Number.isSafeInteger(page) && page > 0 ? page : 1;
  return getChipHistory(safePage);
}
