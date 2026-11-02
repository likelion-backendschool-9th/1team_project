"use server";

import { redirect } from "next/navigation";
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

// TODO(백엔드 연동): 로그아웃 API가 명세에 생기면 토큰 폐기 요청을 함께 보낸다.
export async function logout() {
  await endSession();
  redirect("/login");
}
