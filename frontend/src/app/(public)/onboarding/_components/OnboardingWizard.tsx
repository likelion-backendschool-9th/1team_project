"use client";

import { useState, useTransition } from "react";
import { completeOnboarding } from "../_actions";
import { onboardingSteps, tagSteps, type OnboardingValues, type TagStepKey } from "../_lib/steps";
import { ProfileStep } from "./ProfileStep";
import { StepHeader } from "./StepHeader";
import { StepProgress } from "./StepProgress";
import { TagStep } from "./TagStep";
import styles from "./OnboardingWizard.module.css";

type Props = {
  defaultNickname: string;
  redirectTo: string;
};

const PROFILE_HEADER = { title: "거의 다 왔어요!", desc: "찍먹에서 쓸 프로필을 완성해 주세요. 닉네임만 필수예요." };

// 온보딩 4단계를 한 화면에서 진행한다. 값은 여기서만 들고 있다가 마지막에 한 번에 저장한다.
export function OnboardingWizard({ defaultNickname, redirectTo }: Props) {
  const [values, setValues] = useState<OnboardingValues>({
    nickname: defaultNickname,
    bio: "",
    termsAgreed: false,
    stack: [],
    fields: [],
    topics: [],
  });
  const [index, setIndex] = useState(0);
  const [message, setMessage] = useState<string | null>(null);
  const [pending, startSaving] = useTransition();

  const step = onboardingSteps[index].key;
  const isLast = index === onboardingSteps.length - 1;

  function update(patch: Partial<OnboardingValues>) {
    setValues((current) => ({ ...current, ...patch }));
  }

  function updateTags(key: TagStepKey, ids: string[]) {
    setValues((current) => ({ ...current, [key]: ids }));
  }

  function goTo(next: number) {
    setMessage(null);
    setIndex(next);
  }

  function handleNext() {
    if (!isLast) {
      goTo(index + 1);
      return;
    }
    // 마지막 단계: 서버에 한 번에 저장한다. 성공하면 redirect라 아래 줄은 실패했을 때만 실행된다.
    startSaving(async () => {
      const result = await completeOnboarding(values, redirectTo);
      setIndex(onboardingSteps.findIndex((item) => item.key === result.step));
      setMessage(result.message);
    });
  }

  const header = step === "profile" ? PROFILE_HEADER : tagSteps[step];

  return (
    <>
      <StepProgress current={index} />
      {/* key가 바뀌면 새로 그려져서 단계마다 슬라이드 효과가 난다 */}
      <div key={step} className={styles.slide}>
        <StepHeader title={header.title} desc={header.desc} />
        {step === "profile" ? (
          <ProfileStep values={values} onChange={update} onNext={handleNext} message={message} />
        ) : (
          <TagStep
            step={step}
            selected={values[step]}
            onChange={(ids) => updateTags(step, ids)}
            onPrev={() => goTo(index - 1)}
            onNext={handleNext}
            isLast={isLast}
            pending={pending}
            message={message}
          />
        )}
      </div>
    </>
  );
}