import Link from "next/link";
import { Avatar } from "@/app/_components/ui/Avatar";
import { ArrowRightIcon, UserIcon } from "@/app/_components/ui/Icons";
import { formatCount } from "@/app/_lib/format";
import type { AppNotification } from "@/app/_lib/types/notification";
import type { MyProfile } from "@/app/_lib/types/user";
import { NotificationBell } from "./NotificationBell";
import { UserBadges } from "./UserBadges";
import styles from "./ProfileCard.module.css";

type Props = {
  me: MyProfile;
  unreadCount: number;
  notifications: AppNotification[];
  now: number;
};

export function ProfileCard({ me, unreadCount, notifications, now }: Props) {
  return (
    <section className={styles.card} aria-label="내 프로필">
      <div className={styles.profile}>
        <Avatar name={me.nickname} size={56} />
        <p className={styles.name}>{me.nickname}</p>
      </div>
      {me.plus && <UserBadges badges={me.badges} />}
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
          <dt>칩</dt>
          <dd>{formatCount(me.chipBalance)}</dd>
        </div>
      </dl>
      <Link href={`/mini-home/${me.id}`} className={styles.home}>
        내 미니홈피 바로가기
        <ArrowRightIcon />
      </Link>
      <div className={styles.actions}>
        <Link href="/my" className={styles.action}>
          <UserIcon />
          내 정보
        </Link>
        <NotificationBell
          notifications={notifications}
          unreadCount={unreadCount}
          now={now}
          className={styles.action}
        />
      </div>
    </section>
  );
}
