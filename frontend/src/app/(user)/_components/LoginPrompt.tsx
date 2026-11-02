"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { ArrowRightIcon } from "@/app/_components/ui/Icons";
import styles from "./LoginPrompt.module.css";

// 비로그인 상태에서 내 정보 카드 자리를 대신한다.
export function LoginPrompt() {
  const pathname = usePathname();
  const href = `/login?redirect=${encodeURIComponent(pathname)}`;

  return (
    <section className={styles.card} aria-labelledby="login-prompt-title">
      <p id="login-prompt-title" className={styles.title}>
        찍먹에 오신 것을 환영해요
      </p>
      <p className={styles.desc}>로그인하면 데모를 올리고, 팔로우하고, 댓글을 남길 수 있어요.</p>
      <Link href={href} className={styles.button}>
        로그인하기
        <ArrowRightIcon />
      </Link>
    </section>
  );
}
