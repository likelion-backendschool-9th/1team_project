import Link from "next/link";
import { Avatar } from "@/app/_components/ui/Avatar";
import { BellIcon, ChevronRightIcon, UserIcon } from "@/app/_components/ui/Icons";
import { formatCount } from "@/app/_lib/format";
import type { MyProfile } from "@/app/_lib/types/user";
import { UserBadges } from "@/app/(user)/_components/UserBadges";
import styles from "./BlogProfileCard.module.css";

type Props = {
  me: MyProfile;
  unreadCount: number;
};

export function BlogProfileCard({ me, unreadCount }: Props) {
  return (
    <section className={styles.card} aria-label="내 프로필">
      <div className={styles.profile}>
        <Avatar name={me.nickname} size={56} shape="square" />
        <p className={styles.name}>{me.nickname}</p>
      </div>
      <UserBadges badges={me.badges} />
      <dl className={styles.stats}>
        <div>
          <dt>팔로워</dt>
          <dd>{formatCount(me.followerCount)}</dd>
        </div>
        <div>
          <dt>팔로잉</dt>
          <dd>{formatCount(me.followingCount)}</dd>
        </div>
        <div className={styles.chip}>
          <dt>보유 칩</dt>
          <dd>{formatCount(me.chipBalance)}</dd>
        </div>
      </dl>
      <Link href="/blog/write" className={styles.write}>
        내 블로그 / 새 연재 쓰기
        <ChevronRightIcon />
      </Link>
      <div className={styles.actions}>
        <Link href="/my" className={styles.action}>
          <UserIcon />
          내 정보
        </Link>
        <Link href="/notifications" className={styles.action}>
          <BellIcon />
          알림
          {unreadCount > 0 && (
            <span className={styles.badge}>
              {unreadCount}
              <span className="sr-only">건 읽지 않음</span>
            </span>
          )}
        </Link>
      </div>
    </section>
  );
}
