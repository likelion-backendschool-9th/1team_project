import Link from "next/link";
import { Suspense } from "react";
import { ArrowRightIcon, GearIcon } from "@/app/_components/ui/Icons";
import { getMe, getUnreadCount } from "@/app/(user)/_lib/api";
import { getSubscribedAuthors } from "../_lib/api";
import { BlogProfileCard } from "./BlogProfileCard";
import { SubscribedAuthors } from "./SubscribedAuthors";
import styles from "./BlogAside.module.css";

async function BlogAsideContent() {
  const [me, unreadCount, authors] = await Promise.all([
    getMe(),
    getUnreadCount(),
    getSubscribedAuthors(),
  ]);

  return (
    <>
      <BlogProfileCard me={me} unreadCount={unreadCount} />
      <SubscribedAuthors authors={authors} />
    </>
  );
}

export function BlogAside() {
  return (
    <aside className={styles.aside} aria-label="내 블로그 정보">
      <Suspense fallback={<p className={styles.loading}>불러오는 중…</p>}>
        <BlogAsideContent />
      </Suspense>
      <section className={styles.guarantee} aria-labelledby="guarantee-title">
        <h2 id="guarantee-title" className={styles.guaranteeTitle}>
          <span aria-hidden="true">🛡️</span>
          찍먹 연재 품질 보장제
        </h2>
        <p className={styles.guaranteeText}>
          유료 칩으로 열람한 기술 연재 코드에 실행 불가능한 오류가 있거나 설명이 미흡한
          경우, 열람 후 24시간 이내 100% 칩 환불을 보장합니다.
        </p>
        <Link href="/my/coins" className={styles.guaranteeLink}>
          환불 규정 및 연재 가이드
          <ArrowRightIcon />
        </Link>
      </section>
      <Link href="/my/settings" className={styles.settings}>
        <GearIcon className={styles.settingsIcon} />
        블로그 피드 환경 설정
      </Link>
    </aside>
  );
}
