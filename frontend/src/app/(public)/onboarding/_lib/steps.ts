import { fieldTags, stackTags, topicTags, type Tag } from "./tags";

export type StepKey = "profile" | "stack" | "fields" | "topics";
export type TagStepKey = Exclude<StepKey, "profile">;

export const onboardingSteps: { key: StepKey; label: string }[] = [
  { key: "profile", label: "기본 정보" },
  { key: "stack", label: "기술 스택" },
  { key: "fields", label: "관심분야" },
  { key: "topics", label: "관심주제" },
];

export const MIN_TAGS = 2;

type TagStepConfig = { title: string; desc: string; max: number; options: Tag[] };

export const tagSteps: Record<TagStepKey, TagStepConfig> = {
  stack: {
    title: "어떤 기술을 쓰나요?",
    desc: "주로 쓰거나 관심 있는 기술을 골라 주세요.",
    max: 10,
    options: stackTags,
  },
  fields: {
    title: "어떤 분야에 관심 있나요?",
    desc: "관심 있는 개발 분야를 골라 주세요.",
    max: 10,
    options: fieldTags,
  },
  topics: {
    title: "어떤 주제를 보고 싶나요?",
    desc: "피드와 글을 추천하는 데 쓰여요.",
    max: 7,
    options: topicTags,
  },
};

export function isTagStep(value: string): value is TagStepKey {
  return value in tagSteps;
}

// 단계 사이에 임시로 보관하는 입력값. 태그는 id로 담는다.
export type OnboardingDraft = {
  nickname?: string;
  bio?: string;
  githubUsername?: string;
  termsAgreed?: boolean;
  stack?: string[];
  fields?: string[];
  topics?: string[];
};

// 고른 태그가 목록에 있고, 중복이 없고, 개수 제한 안인지 확인한다.
export function validateTags(step: TagStepKey, ids: string[]): string | null {
  const { max, options } = tagSteps[step];
  const valid = new Set(options.map((tag) => tag.id));
  if (ids.some((id) => !valid.has(id)) || new Set(ids).size !== ids.length)
    return "목록에 없는 태그가 있어요. 다시 골라 주세요.";
  if (ids.length < MIN_TAGS) return `${MIN_TAGS}개 이상 골라 주세요.`;
  if (ids.length > max) return `최대 ${max}개까지 고를 수 있어요.`;
  return null;
}

export function isStepDone(draft: OnboardingDraft, step: StepKey): boolean {
  if (step === "profile") return Boolean(draft.nickname && draft.termsAgreed);
  return validateTags(step, draft[step] ?? []) === null;
}

// 아직 끝나지 않은 첫 단계. before를 주면 그 단계 앞쪽만 본다.
export function firstIncompleteStep(draft: OnboardingDraft, before?: StepKey): StepKey | null {
  for (const { key } of onboardingSteps) {
    if (key === before) return null;
    if (!isStepDone(draft, key)) return key;
  }
  return null;
}

export function nextStep(step: StepKey): StepKey | null {
  const index = onboardingSteps.findIndex((item) => item.key === step);
  return onboardingSteps[index + 1]?.key ?? null;
}

export function previousStep(step: StepKey): StepKey | null {
  const index = onboardingSteps.findIndex((item) => item.key === step);
  return onboardingSteps[index - 1]?.key ?? null;
}

export function stepUrl(step: StepKey, redirectTo: string): string {
  const base = `/onboarding/${step}`;
  return redirectTo === "/main" ? base : `${base}?redirect=${encodeURIComponent(redirectTo)}`;
}
