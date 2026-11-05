"use server";

import { redirect } from "next/navigation";
import { startMockSession } from "@/app/(user)/_lib/session";
import { ApiRequestError, hasBackend } from "@/app/_lib/api/client";
import { parseRedirect } from "@/app/_lib/redirect";
import { completeOnboardingProfile, isNicknameTaken } from "./_lib/api";
import { BIO_MAX, validateNickname } from "./_lib/rules";
import { validateTags, type OnboardingValues, type StepKey } from "./_lib/steps";

export type NicknameCheck = { ok: boolean; message: string };

export async function checkNickname(nickname: string): Promise<NicknameCheck> {
  const trimmed = nickname.trim();
  const invalid = validateNickname(trimmed);
  if (invalid) return { ok: false, message: invalid };
  if (await isNicknameTaken(trimmed)) return { ok: false, message: "이미 사용 중인 닉네임이에요." };
  return { ok: true, message: "사용할 수 있는 닉네임이에요." };
}

/** 실패하면 어느 단계로 돌아가야 하는지 함께 알려 준다. 성공하면 redirect라 돌아오지 않는다. */
export type CompleteResult = { message: string; step: StepKey };

// 클라이언트가 보낸 값은 모양부터 믿지 않는다.
const toList = (value: unknown): string[] => (Array.isArray(value) ? value.map(String) : []);

/** 온보딩 완료: 4단계 값을 서버에서 다시 검증하고 백엔드에 JSON으로 한 번에 저장한다. */
export async function completeOnboarding(values: OnboardingValues, redirectTo: string): Promise<CompleteResult> {
  const nickname = String(values.nickname ?? "").trim();
  const bio = String(values.bio ?? "").trim();
  const stack = toList(values.stack);
  const fields = toList(values.fields);
  const topics = toList(values.topics);

  const nicknameError = validateNickname(nickname);
  if (nicknameError) return { message: nicknameError, step: "profile" };
  if (await isNicknameTaken(nickname)) return { message: "이미 사용 중인 닉네임이에요.", step: "profile" };
  if (bio.length > BIO_MAX) return { message: `자기소개는 ${BIO_MAX}자 이하로 입력해 주세요.`, step: "profile" };
  if (values.termsAgreed !== true) return { message: "이용약관과 개인정보 처리방침에 동의해 주세요.", step: "profile" };

  const tagLists = { stack, fields, topics };
  for (const step of ["stack", "fields", "topics"] as const) {
    const error = validateTags(step, tagLists[step]);
    if (error) return { message: error, step };
  }

  const target = parseRedirect(redirectTo);

  if (!hasBackend) {
    await startMockSession();
    redirect(target);
  }

  try {
    await completeOnboardingProfile({ nickname, bio, techStacks: stack, interestTags: [...fields, ...topics] });
  } catch (err) {
    const message =
      err instanceof ApiRequestError ? err.error.message : "가입 정보 저장에 실패했어요. 다시 시도해 주세요.";
    return { message, step: "topics" };
  }

  redirect(target);
}