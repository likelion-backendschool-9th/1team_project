import Link from "next/link";
import { Avatar } from "@/app/_components/ui/Avatar";
import { ChevronRightIcon } from "@/app/_components/ui/Icons";
import type { Following } from "@/app/_lib/types/user";
import styles from "./FollowingList.module.css";

type Props = {
  followings: Following[];
};

export function FollowingList({ followings }: Props) {
  return (
    <section aria-labelledby="following-title">
      <div className={styles.header}>
        <h2 id="following-title" className={styles.title}>
          팔로잉
        </h2>
        <Link href="/my/follows" className={styles.more}>
          전체 보기
          <ChevronRightIcon />
        </Link>
      </div>
      <ul className={styles.list}>
        {followings.map((user) => (
          <li key={user.id}>
            <Link href={`/mini-home/${user.id}`} className={styles.row}>
              <Avatar name={user.nickname} size={40} tone="light" />
              <span className={styles.name}>@{user.nickname}</span>
              <span
                className={`${styles.dot} ${user.online ? styles.online : ""}`}
                role="img"
                aria-label={user.online ? "접속 중" : "오프라인"}
              />
            </Link>
          </li>
        ))}
      </ul>
    </section>
  );
}
