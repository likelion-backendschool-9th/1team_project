import type { Metadata } from "next";
import Link from "next/link";
import type { ReactNode } from "react";
import { LogoMark } from "@/app/_components/ui/LogoMark";
import { AdminNav } from "./_components/AdminNav";
import styles from "./_components/Admin.module.css";

export const metadata: Metadata = {
  title: { default: "관리자", template: "%s | 찍먹 관리자" },
  robots: { index: false, follow: false },
};

export default function AdminLayout({ children }: { children: ReactNode }) {
  return (
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
        <AdminNav />
      </header>
      <main id="main-content" tabIndex={-1} className={styles.main}>
        {children}
      </main>
    </div>
  );
}
