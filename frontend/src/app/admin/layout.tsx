import type { Metadata } from "next";
import Link from "next/link";
import { Suspense, type ReactNode } from "react";
import { LogoMark } from "@/app/_components/ui/LogoMark";
import { AdminGate } from "./_components/AdminGate";
import { AdminNav } from "./_components/AdminNav";
import styles from "./_components/Admin.module.css";

export const metadata: Metadata = {
  title: { default: "관리자", template: "%s | 찍먹 관리자" },
  robots: { index: false, follow: false },
};

export default function AdminLayout({ children }: { children: ReactNode }) {
  // 권한 확인이 끝날 때까지는 관리 메뉴를 그리지 않는다. 관리자가 아니면 AdminGate가 차단 안내를 보여 준다.
  return (
    <Suspense fallback={<p role="status" aria-busy="true" style={{ padding: 32 }}>권한을 확인하는 중…</p>}>
      <AdminGate>
        {(master) => (
        <div className={styles.shell}>
          <header className={styles.header}>
            <div className={styles.headerTop}>
              <Link href="/admin" className={styles.logo}>
                <LogoMark size={30} />
                <strong>찍먹</strong>
                <span className={styles.logoSub}>ADMIN</span>
              </Link>
              <Link href="/main" className={styles.back}>
                서비스로 돌아가기 →
              </Link>
            </div>
            <AdminNav master={master} />
          </header>
          <main id="main-content" tabIndex={-1} className={styles.main}>
            {children}
          </main>
        </div>
        )}
      </AdminGate>
    </Suspense>
  );
}
