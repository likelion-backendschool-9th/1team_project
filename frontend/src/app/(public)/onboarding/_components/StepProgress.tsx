"use client";

import { usePathname } from "next/navigation";
import { onboardingSteps } from "../_lib/steps";
import styles from "./StepProgress.module.css";

// 가입 단계 표시. 지금 단계는 주소로 판단한다.
export function StepProgress() {
  const pathname = usePathname();
  const current = onboardingSteps.findIndex((step) => pathname.startsWith(`/onboarding/${step.key}`));

  return (
    <nav aria-label="가입 단계">
      <ol className={styles.list}>
        {onboardingSteps.map((step, index) => (
          <li
            key={step.key}
            className={`${styles.item} ${index <= current ? styles.reached : ""}`}
            aria-current={index === current ? "step" : undefined}
          >
            <span className={styles.bar} aria-hidden="true" />
            <span className={styles.label}>
              {index + 1}. {step.label}
            </span>
          </li>
        ))}
      </ol>
    </nav>
  );
}
