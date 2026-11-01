"use server";

import { revalidatePath } from "next/cache";
import { runForm } from "@/app/_lib/api/formAction";
import { mutate } from "@/app/_lib/api/mutate";
import { readValues, type FormState } from "@/app/_lib/formState";

const TYPES = ["question", "bug", "praise"];

export async function playDemo(demoId: number) {
  return mutate(`/api/v1/demos/${demoId}/play`, "POST", undefined, "샌드박스를 열었어요.");
}

export async function toggleDemoLike(demoId: number, like: boolean) {
  return mutate(`/api/v1/demos/${demoId}/like`, like ? "POST" : "DELETE", undefined, "좋아요를 반영했어요.");
}

export async function reportDemo(demoId: number) {
  return mutate(`/api/v1/demos/${demoId}/report`, "POST", { reason: "기타" }, "신고를 접수했어요.");
}

export async function createDemoComment(
  demoId: number,
  previous: FormState,
  formData: FormData,
): Promise<FormState> {
  const values = readValues(formData, ["content", "type"]);
  const errors: Record<string, string> = {};
  if (!values.content) errors.content = "댓글을 입력해 주세요.";
  else if (values.content.length > 300) errors.content = "300자 이하로 입력해 주세요.";
  if (!TYPES.includes(values.type)) errors.type = "피드백 종류를 골라 주세요.";

  const state = await runForm(previous, values, errors, () =>
    mutate(`/api/v1/demos/${demoId}/comments`, "POST", values, "피드백을 남겼어요."),
  );
  if (state.ok) revalidatePath(`/demos/${demoId}`);
  return state.ok ? { ...state, values: { type: values.type } } : state;
}
