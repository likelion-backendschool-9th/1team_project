import type { Metadata } from "next";
import styles from "./_components/Dm.module.css";

export const metadata: Metadata = { title: "메시지", robots: { index: false } };

export default function DmPage() {
  return (
    <div className={styles.placeholder}>
      <span className={styles.placeholderIcon} aria-hidden="true">
        💬
      </span>
      <h1 className={styles.placeholderTitle}>메시지</h1>
      <p>왼쪽에서 대화를 고르거나, 미니홈피에서 DM 보내기를 눌러 대화를 시작해 보세요.</p>
    </div>
  );
}
