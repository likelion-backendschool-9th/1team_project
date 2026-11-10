import type { Metadata } from "next";
import { GoodbyeDialog } from "./_components/GoodbyeDialog";
import styles from "./page.module.css";

export const metadata: Metadata = { title: "탈퇴 완료", robots: { index: false } };

export default function GoodbyePage() {
  return (
    <main id="main-content" className={styles.page}>
      <GoodbyeDialog />
    </main>
  );
}