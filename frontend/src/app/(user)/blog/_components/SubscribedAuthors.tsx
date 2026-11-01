import Link from "next/link";
import { Avatar } from "@/app/_components/ui/Avatar";
import { ChevronRightIcon } from "@/app/_components/ui/Icons";
import type { SubscribedAuthor } from "@/app/_lib/types/post";
import styles from "./SubscribedAuthors.module.css";

type Props = {
  authors: SubscribedAuthor[];
};

export function SubscribedAuthors({ authors }: Props) {
  return (
    <section className={styles.card} aria-labelledby="subscribed-title">
      <div className={styles.header}>
        <h2 id="subscribed-title" className={styles.title}>
          구독 중인 연재 작가
        </h2>
        <Link href="/my/subscription" className={styles.more}>
          전체 보기
          <ChevronRightIcon />
        </Link>
      </div>
      <ul className={styles.list}>
        {authors.map(({ user, seriesLabel, online }) => (
          <li key={user.id}>
            <Link href={`/blog/${user.id}`} className={styles.row}>
              <Avatar name={user.nickname} size={40} tone="light" />
              <span className={styles.info}>
                <span className={styles.name}>@{user.nickname}</span>
                <span className={styles.series}>{seriesLabel}</span>
              </span>
              <span
                className={`${styles.dot} ${online ? styles.online : ""}`}
                role="img"
                aria-label={online ? "접속 중" : "오프라인"}
              />
            </Link>
          </li>
        ))}
      </ul>
    </section>
  );
}
