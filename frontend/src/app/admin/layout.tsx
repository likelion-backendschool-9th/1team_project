import type { Metadata } from "next";
import Link from "next/link";
import type { ReactNode } from "react";
import { AdminNav } from "./_components/AdminNav";
import styles from "./_components/Admin.module.css";

export const metadata: Metadata = {
  title: { default: "관리자", template: "%s | 찍먹 관리자" },
  robots: { index: false, follow: false },
};

export default function AdminLayout({ children }: { children: ReactNode }) {
  return (
    <div className={styles.shell}>
      <aside className={styles.sidebar} aria-label="관리자 사이드바">
        <Link href="/admin" className={styles.logo}>
          <span className={styles.mark} aria-hidden="true" />
          <span>
            <strong>찍먹</strong>
            <span className={styles.logoSub}>ADMIN</span>
          </span>
        </Link>
        <AdminNav />
        <Link href="/main" className={styles.back}>
          ← 서비스로 돌아가기
        </Link>
      </aside>
      <main id="main-content" tabIndex={-1} className={styles.main}>
        {children}
      </main>
    </div>
  );
}
