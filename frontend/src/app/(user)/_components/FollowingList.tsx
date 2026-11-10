import Link from "next/link";
import { Avatar } from "@/app/_components/ui/Avatar";
import { ChatIcon, ChevronRightIcon } from "@/app/_components/ui/Icons";
import type { Following } from "@/app/_lib/types/user";
import { DmOpenButton } from "./DmOpenButton";
import { UserBadges } from "./UserBadges";
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
      <ul className={styles.list} aria-label={`팔로잉 ${followings.length}명`} tabIndex={0}>
        {followings.map((user) => (
          <li key={user.id} className={styles.item}>
            <Link href={`/mini-home/${user.id}`} className={styles.row}>
              <Avatar name={user.nickname} src={user.profileImage} size={32} tone="light" />
              <div className={styles.info}>
                <span className={styles.name}>@{user.nickname}</span>
                <UserBadges badges={user.badges} size="sm" />
              </div>
              <span
                className={`${styles.dot} ${user.online ? styles.online : ""}`}
                role="img"
                aria-label={user.online ? "접속 중" : "오프라인"}
              />
            </Link>
            <DmOpenButton user={user} className={styles.dm} aria-label={`@${user.nickname}에게 DM 보내기`}>
              <ChatIcon aria-hidden="true" />
            </DmOpenButton>
          </li>
        ))}
      </ul>
    </section>
  );
}
