"use server";

import { revalidatePath } from "next/cache";
import { runForm } from "@/app/_lib/api/formAction";
import { mutate } from "@/app/_lib/api/mutate";
import { readValues, type FormState } from "@/app/_lib/formState";

export async function createComment(
  feedId: number,
  previous: FormState,
  formData: FormData,
): Promise<FormState> {
  const values = readValues(formData, ["content"]);
  const errors: Record<string, string> = {};
  if (!values.content) errors.content = "댓글을 입력해 주세요.";
  else if (values.content.length > 300) errors.content = "300자 이하로 입력해 주세요.";

  const state = await runForm(previous, values, errors, () =>
    mutate(`/api/v1/feeds/${feedId}/comments`, "POST", values, "댓글을 남겼어요."),
  );
  if (state.ok) revalidatePath(`/feeds/${feedId}`);
  return state.ok ? { ...state, values: {} } : state;
}
