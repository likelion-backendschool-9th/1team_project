import Link from "next/link";
import { connection } from "next/server";
import type { ReactNode } from "react";
import { LogoMark } from "@/app/_components/ui/LogoMark";
import { getAdminAccess } from "../_lib/access";
import styles from "./AdminGate.module.css";

type Props = {
  children: ReactNode;
};

// 관리자(ADMIN)가 아니면 관리 메뉴·화면 대신 차단 안내만 보여 준다.
export async function AdminGate({ children }: Props) {
  await connection();
  const access = await getAdminAccess();
  if (access === "admin") return children;

  const guest = access === "guest";
  return (
    <main id="main-content" className={styles.page}>
      <section className={styles.card} role="alert" aria-labelledby="admin-block-title">
        <span className={styles.lock} aria-hidden="true">
          🔒
        </span>
        <LogoMark size={36} className={styles.mark} />
        <h1 id="admin-block-title" className={styles.title}>
          {guest ? "관리자 로그인이 필요해요" : "관리자만 볼 수 있는 페이지예요"}
        </h1>
        <p className={styles.text}>
          {guest
            ? "관리자 계정으로 로그인한 뒤 다시 들어와 주세요."
            : "이 계정에는 관리자 권한이 없어요. 권한이 필요하면 운영팀에 요청해 주세요."}
        </p>
        <div className={styles.actions}>
          {guest && (
            <Link href="/login?redirect=%2Fadmin" className={styles.primary}>
              로그인하기
            </Link>
          )}
          <Link href="/main" className={guest ? styles.secondary : styles.primary}>
            서비스로 돌아가기
          </Link>
        </div>
      </section>
    </main>
  );
}
