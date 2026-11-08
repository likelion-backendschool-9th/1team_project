"use server";

import { revalidatePath } from "next/cache";
import { mutate, type MutationResult } from "@/app/_lib/api/mutate";

const TARGET_TYPES = new Set(["post", "demo", "user", "feed"]);

function isTarget(type: unknown, id: unknown): type is string {
  return typeof type === "string" && TARGET_TYPES.has(type) && Number.isSafeInteger(id) && (id as number) > 0;
}

/**
 * 검색 결과 숨김. 로그인 여부는 백엔드가 JWT 쿠키로 확인한다(비로그인이면 401).
 */
export async function hideSearchResult(type: unknown, id: unknown): Promise<MutationResult> {
  if (!isTarget(type, id)) return { ok: false, message: "잘못된 요청이에요." };
  const result = await mutate("/api/v1/search/hidden", "POST", { targetType: type, targetId: id }, "검색 결과에서 숨겼어요.");
  if (result.ok) revalidatePath("/search");
  return result;
}

/** 검색 결과로 들어간 방문을 기록한다. 개인화 보조 기능이라 실패해도 화면에 알리지 않는다 */
export async function recordSearchVisit(type: unknown, id: unknown, query: unknown): Promise<void> {
  if (!isTarget(type, id) || typeof query !== "string" || !query.trim() || query.length > 100) return;
  await mutate("/api/v1/search/visits", "POST", { targetType: type, targetId: id, query }, "");
}
