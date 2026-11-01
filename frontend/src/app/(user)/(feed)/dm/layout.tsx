import type { ReactNode } from "react";
import { Suspense } from "react";
import { RoomList } from "./_components/RoomList";
import styles from "./_components/Dm.module.css";

export default function DmLayout({ children }: { children: ReactNode }) {
  return (
    <div className={styles.layout}>
      <Suspense fallback={<p className={styles.loading}>대화 목록을 불러오는 중…</p>}>
        <RoomList />
      </Suspense>
      <div className={styles.panel}>{children}</div>
    </div>
  );
}
