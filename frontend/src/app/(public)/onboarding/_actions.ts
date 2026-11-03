"use server";

import { redirect } from "next/navigation";
import { startMockSession } from "@/app/(user)/_lib/session";
import { ApiRequestError, isMockMode } from "@/app/_lib/api/client";
import { readValues, type FormState } from "@/app/_lib/formState";
import { parseRedirect } from "@/app/_lib/redirect";
import { completeOnboardingProfile, isNicknameTaken } from "./_lib/api";
import { clearDraft, saveDraft } from "./_lib/draft";
import { BIO_MAX, validateGithubUsername, validateNickname } from "./_lib/rules";
import { firstIncompleteStep, isTagStep, nextStep, stepUrl, validateTags } from "./_lib/steps";

export type NicknameCheck = { ok: boolean; message: string };

export async function checkNickname(nickname: string): Promise<NicknameCheck> {
  const trimmed = nickname.trim();
  const invalid = validateNickname(trimmed);
  if (invalid) return { ok: false, message: invalid };
  if (await isNicknameTaken(trimmed)) return { ok: false, message: "이미 사용 중인 닉네임이에요." };
  return { ok: true, message: "사용할 수 있는 닉네임이에요." };
}

/** 1단계: 기본 정보. 통과하면 draft 쿠키에 보관하고 다음 단계로 간다. */
export async function saveProfile(previous: FormState, formData: FormData): Promise<FormState> {
  const values = readValues(formData, ["nickname", "bio", "githubUsername", "redirect"]);
  const attempt = previous.attempt + 1;
  const errors: Record<string, string> = {};

  const nicknameError = validateNickname(values.nickname);
  if (nicknameError) errors.nickname = nicknameError;
  else if (await isNicknameTaken(values.nickname)) errors.nickname = "이미 사용 중인 닉네임이에요.";

  if (values.bio.length > BIO_MAX) errors.bio = `${BIO_MAX}자 이하로 입력해 주세요.`;

  const githubError = validateGithubUsername(values.githubUsername);
  if (githubError) errors.githubUsername = githubError;

  if (formData.get("terms") !== "on") errors.terms = "이용약관과 개인정보 처리방침에 동의해 주세요.";

  if (Object.keys(errors).length > 0)
    return { ok: false, message: "입력값을 확인해 주세요.", errors, values, attempt };

  await saveDraft({
    nickname: values.nickname,
    bio: values.bio,
    githubUsername: values.githubUsername,
    termsAgreed: true,
  });
  redirect(stepUrl("stack", parseRedirect(values.redirect)));
}

/** 2~4단계: 기술 스택·관심분야·관심주제. 마지막에서 백엔드에 한 번 저장한다. */
export async function saveTagStep(step: string, previous: FormState, formData: FormData): Promise<FormState> {
  const values = readValues(formData, ["redirect"]);
  const attempt = previous.attempt + 1;
  if (!isTagStep(step)) redirect("/onboarding");
  const redirectTo = parseRedirect(values.redirect);

  const ids = formData.getAll("tags").map(String);
  const error = validateTags(step, ids);
  if (error) return { ok: false, message: error, errors: { tags: error }, values, attempt };

  const draft = await saveDraft({ [step]: ids });
  const next = nextStep(step);
  if (next) redirect(stepUrl(next, redirectTo));

  // 마지막 단계: 앞 단계가 비어 있으면 그 단계로 돌려보낸다.
  const missing = firstIncompleteStep(draft);
  if (missing) redirect(stepUrl(missing, redirectTo));

  if (isMockMode) {
    await startMockSession();
    await clearDraft();
    redirect(redirectTo);
  }

  // 실서버: draft 전체를 온보딩 저장 API로 보낸다.
  try {
    await completeOnboardingProfile({
      nickname: draft.nickname ?? "",
      bio: draft.bio ?? "",
      githubUsername: draft.githubUsername ?? "",
      techStacks: draft.stack ?? [],
      interestTags: [...(draft.fields ?? []), ...(draft.topics ?? [])],
    });
  } catch (err) {
    const message =
      err instanceof ApiRequestError ? err.error.message : "가입 정보 저장에 실패했어요. 다시 시도해 주세요.";
    return { ok: false, message, errors: {}, values, attempt };
  }

  await clearDraft();
  redirect(redirectTo);
}
