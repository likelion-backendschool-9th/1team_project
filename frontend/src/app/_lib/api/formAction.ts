import "server-only";
import type { FormState } from "@/app/_lib/formState";
import type { MutationResult } from "./mutate";

// 폼 Server Action 공통 흐름: 검증 오류가 있으면 입력값과 함께 돌려주고, 없으면 요청을 보낸다.
export async function runForm(
  previous: FormState,
  values: Record<string, string>,
  errors: Record<string, string>,
  send: () => Promise<MutationResult>,
): Promise<FormState> {
  const attempt = previous.attempt + 1;
  if (Object.keys(errors).length > 0)
    return { ok: false, message: "입력값을 확인해 주세요.", errors, values, attempt };

  const result = await send();
  return { ok: result.ok, message: result.message, errors: {}, values, attempt };
}
