import { onboardingSteps } from "../_lib/steps";
import styles from "./StepProgress.module.css";

type Props = { current: number };

// 가입 단계 표시. 지금 단계 순번은 Wizard가 알려 준다.
export function StepProgress({ current }: Props) {
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