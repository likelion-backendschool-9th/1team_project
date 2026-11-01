"use server";

import { isMockMode } from "@/app/_lib/api/client";
import { readValues, type FormState } from "@/app/_lib/formState";
import { MAX_DEMO_MB } from "./_lib/rules";

export async function uploadDemo(previous: FormState, formData: FormData): Promise<FormState> {
  const values = readValues(formData, ["title", "description", "tags", "runtime"]);
  const file = formData.get("file");
  const errors: Record<string, string> = {};
  const attempt = previous.attempt + 1;

  if (!values.title) errors.title = "데모 이름을 입력해 주세요.";
  else if (values.title.length > 40) errors.title = "40자 이하로 입력해 주세요.";
  if (values.description.length > 200) errors.description = "200자 이하로 입력해 주세요.";
  if (!(file instanceof File) || file.size === 0) errors.file = "zip 파일을 선택해 주세요.";
  else if (!file.name.toLowerCase().endsWith(".zip")) errors.file = "zip 파일만 올릴 수 있어요.";
  else if (file.size > MAX_DEMO_MB * 1024 * 1024) errors.file = `${MAX_DEMO_MB}MB 이하만 올릴 수 있어요.`;

  if (Object.keys(errors).length > 0)
    return { ok: false, message: "입력값을 확인해 주세요.", errors, values, attempt };

  // 업로드는 multipart 전송이라 백엔드가 준비되면 별도 요청기로 연결한다.
  if (!isMockMode)
    return { ok: false, message: "업로드 API 연결 전이에요.", errors: {}, values, attempt };

  return {
    ok: true,
    message: "업로드했어요. 파일 검사가 끝나면 데모가 공개돼요. (백엔드 연결 전이라 실제로 저장되지는 않아요)",
    errors: {},
    values: {},
    attempt,
  };
}
