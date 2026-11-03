"use server";

import { redirect } from "next/navigation";
import { apiFetch, isMockMode } from "@/app/_lib/api/client";
import { mutate } from "@/app/_lib/api/mutate";
import { endSession } from "./_lib/session";

export async function updatePresence() {
  return mutate("/api/v1/users/me/presence", "PUT", { online: true }, "접속 상태를 갱신했어요.");
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
  redirect("/main");
}
