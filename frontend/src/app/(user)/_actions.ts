"use server";

import { redirect } from "next/navigation";
import { apiFetch, hasBackend, isMockMode } from "@/app/_lib/api/client";
import { mutate } from "@/app/_lib/api/mutate";
import type { DmMessage, DmRoom } from "@/app/_lib/types/dm";
import type { AutocompleteSuggestion } from "@/app/_lib/types/search";
import { getMe, getServerTime } from "./_lib/api";
import { getAutocomplete } from "./_lib/autocomplete";
import { getMessages, getRooms } from "./_lib/dmApi";
import { endSession } from "./_lib/session";

export async function updatePresence() {
  return mutate("/api/v1/users/me/presence", "PUT", { online: true }, "접속 상태를 갱신했어요.", !hasBackend);
}

// DM 창은 어느 화면에서나 열고 닫는 클라이언트 창이라, 열 때 이 Action으로 데이터를 받아 온다.
export async function loadDmRooms(): Promise<{ myId: number; rooms: DmRoom[]; now: number }> {
  const me = await getMe();
  if (!me) redirect("/login");
  const [rooms, now] = await Promise.all([getRooms(), getServerTime()]);
  return { myId: me.id, rooms, now };
}

/** 대화 내역. 백엔드는 이 조회로 대화방을 읽음 처리한다. */
export async function loadDmMessages(myId: number, partnerId: number): Promise<DmMessage[]> {
  if (!Number.isSafeInteger(partnerId) || partnerId <= 0) return [];
  return getMessages(myId, partnerId);
}

/**
 * 검색창 자동완성 후보. 검색창은 모든 화면에 있는 클라이언트 입력이라 이 Action으로 후보를 받아 온다.
 * 검색은 비로그인도 쓰는 공개 기능이라 권한 확인이 없다. 실패하면 빈 목록을 줘서 검색창은 그대로 쓸 수 있게 한다.
 */
export async function loadAutocomplete(q: string): Promise<AutocompleteSuggestion[]> {
  if (typeof q !== "string") return [];
  const query = q.trim();
  if (!query || query.length > 50) return [];
  try {
    return await getAutocomplete(query);
  } catch {
    return [];
  }
}

export async function markNotificationsRead(ids: number[]) {
  const valid = ids.filter((id) => Number.isSafeInteger(id) && id > 0);
  const results = await Promise.all(
    valid.map((id) => mutate(`/api/v1/notifications/${id}/read`, "PUT", undefined, "읽음 처리했어요.")),
  );
  return results.find((result) => !result.ok) ?? { ok: true, message: "읽음 처리했어요." };
}

/**
 * 로그아웃 Action.
 * 실서버면 백엔드에 쿠키 만료를 요청한 뒤, Next 쪽 쿠키도 지우고 메인으로 이동한다.
 */
export async function logout() {
  if (!isMockMode) {
    try {
      await apiFetch("/api/v1/auth/logout", { method: "POST" });
    } catch {
      // 토큰이 이미 만료돼도 로컬 세션 정리는 계속한다.
    }
  }
  await endSession();
  redirect("/");
}
