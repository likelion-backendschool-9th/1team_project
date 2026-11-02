import type { ReactNode } from "react";
import styles from "./template.module.css";

// 단계를 옮길 때마다 새로 그려져서 슬라이드처럼 들어오는 효과를 준다.
export default function OnboardingTemplate({ children }: { children: ReactNode }) {
  return <div className={styles.slide}>{children}</div>;
}
