import "server-only";
import { cookies } from "next/headers";
import type { OnboardingDraft } from "./steps";

const DRAFT_COOKIE = "onboarding_draft";
const ONE_DAY = 60 * 60 * 24;

// 단계 사이에 입력값을 들고 가기 위한 임시 쿠키. 비밀번호·토큰은 담지 않는다.
// TODO(백엔드 연동): 단계마다 저장할지, 마지막에 한 번에 보낼지 정해지면 교체한다.
export async function readDraft(): Promise<OnboardingDraft> {
  const raw = (await cookies()).get(DRAFT_COOKIE)?.value;
  if (!raw) return {};
  try {
    const parsed: unknown = JSON.parse(raw);
    return parsed && typeof parsed === "object" ? (parsed as OnboardingDraft) : {};
  } catch {
    return {};
  }
}

export async function saveDraft(patch: OnboardingDraft): Promise<OnboardingDraft> {
  const next = { ...(await readDraft()), ...patch };
  (await cookies()).set(DRAFT_COOKIE, JSON.stringify(next), {
    path: "/",
    httpOnly: true,
    sameSite: "lax",
    maxAge: ONE_DAY,
  });
  return next;
}

export async function clearDraft(): Promise<void> {
  (await cookies()).delete(DRAFT_COOKIE);
}
