"use server";

import { mutate } from "@/app/_lib/api/mutate";

export async function markRead(ids: number[]) {
  const valid = ids.filter((id) => Number.isSafeInteger(id) && id > 0);
  const results = await Promise.all(
    valid.map((id) => mutate(`/api/v1/notifications/${id}/read`, "PUT", undefined, "읽음 처리했어요.")),
  );
  return results.find((result) => !result.ok) ?? { ok: true, message: "읽음 처리했어요." };
}
