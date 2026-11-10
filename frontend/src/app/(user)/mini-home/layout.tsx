import type { ReactNode } from "react";
import styles from "./layout.module.css";

export default function MiniHomeLayout({ children }: { children: ReactNode }) {
  return (
    <main id="main-content" tabIndex={-1} className={styles.main}>
      {children}
    </main>
  );
}
