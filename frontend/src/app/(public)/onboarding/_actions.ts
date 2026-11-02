"use server";

import { redirect } from "next/navigation";
import { startMockSession } from "@/app/(user)/_lib/session";
import { isMockMode } from "@/app/_lib/api/client";
import { readValues, type FormState } from "@/app/_lib/formState";
import { parseRedirect } from "@/app/_lib/redirect";
import { isNicknameTaken } from "./_lib/api";
import {
  BIO_MAX,
  MAX_STACKS,
  STACK_MAX_LENGTH,
  parseStacks,
  validateGithubUsername,
  validateNickname,
} from "./_lib/rules";

export type NicknameCheck = { ok: boolean; message: string };

export async function checkNickname(nickname: string): Promise<NicknameCheck> {
  const trimmed = nickname.trim();
  const invalid = validateNickname(trimmed);
  if (invalid) return { ok: false, message: invalid };
  if (await isNicknameTaken(trimmed)) return { ok: false, message: "이미 사용 중인 닉네임이에요." };
  return { ok: true, message: "사용할 수 있는 닉네임이에요." };
}

export async function submitOnboarding(previous: FormState, formData: FormData): Promise<FormState> {
  const values = readValues(formData, ["nickname", "bio", "techStacks", "githubUsername", "redirect"]);
  const attempt = previous.attempt + 1;
  const errors: Record<string, string> = {};

  const nicknameError = validateNickname(values.nickname);
  if (nicknameError) errors.nickname = nicknameError;
  else if (await isNicknameTaken(values.nickname)) errors.nickname = "이미 사용 중인 닉네임이에요.";

  if (values.bio.length > BIO_MAX) errors.bio = `${BIO_MAX}자 이하로 입력해 주세요.`;

  const stacks = parseStacks(values.techStacks);
  if (stacks.length > MAX_STACKS) errors.techStacks = `기술 스택은 ${MAX_STACKS}개까지 고를 수 있어요.`;
  else if (stacks.some((stack) => stack.length > STACK_MAX_LENGTH))
    errors.techStacks = `기술 스택 이름은 ${STACK_MAX_LENGTH}자 이하로 입력해 주세요.`;

  const githubError = validateGithubUsername(values.githubUsername);
  if (githubError) errors.githubUsername = githubError;

  if (formData.get("terms") !== "on") errors.terms = "이용약관과 개인정보 처리방침에 동의해 주세요.";

  if (Object.keys(errors).length > 0)
    return { ok: false, message: "입력값을 확인해 주세요.", errors, values, attempt };

  // TODO(백엔드 연동): 기획서에 온보딩 저장 API가 생기면 mutate()로 연결한다.
  if (!isMockMode)
    return { ok: false, message: "가입 정보 저장 API 연결 전이에요.", errors: {}, values, attempt };

  await startMockSession();
  redirect(parseRedirect(values.redirect));
}
