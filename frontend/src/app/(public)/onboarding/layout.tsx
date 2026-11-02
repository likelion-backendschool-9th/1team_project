import type { ReactNode } from "react";
import { Brand } from "@/app/(user)/_components/Brand";
import { StepProgress } from "./_components/StepProgress";
import styles from "./layout.module.css";

export default function OnboardingLayout({ children }: { children: ReactNode }) {
  return (
    <main id="main-content" tabIndex={-1} className={styles.page}>
      <div className={styles.inner}>
        <Brand />
        <StepProgress />
        {children}
      </div>
    </main>
  );
}
