import Link from "next/link";
import { Avatar } from "@/app/_components/ui/Avatar";
import { ChipIcon } from "@/app/_components/ui/ChipIcon";
import { ChevronRightIcon, UserIcon } from "@/app/_components/ui/Icons";
import { formatCount } from "@/app/_lib/format";
import type { AppNotification } from "@/app/_lib/types/notification";
import type { MyProfile } from "@/app/_lib/types/user";
import { NotificationBell } from "@/app/(user)/_components/NotificationBell";
import { UserBadges } from "@/app/(user)/_components/UserBadges";
import { LogoutButton } from "@/app/(user)/_components/LogoutButton";
import styles from "./BlogProfileCard.module.css";

type Props = {
  me: MyProfile;
  unreadCount: number;
  notifications: AppNotification[];
  now: number;
};

export function BlogProfileCard({ me, unreadCount, notifications, now }: Props) {
  return (
    <section className={styles.card} aria-label="내 프로필">
      <div className={styles.profile}>
        <Avatar name={me.nickname} src={me.profileImage} size={48} />
        <div className={styles.who}>
          <p className={styles.name}>{me.nickname}</p>
          <LogoutButton />
        </div>
      </div>
      {me.plus && <UserBadges badges={me.badges} />}
      <dl className={styles.stats}>
        <div>
          <dt>팔로워</dt>
          <dd>
            <Link href="/my/follows?tab=followers" className={styles.statLink} aria-label={`팔로워 ${formatCount(me.followerCount)}명 목록 보기`}>
              {formatCount(me.followerCount)}
            </Link>
          </dd>
        </div>
        <div>
          <dt>팔로잉</dt>
          <dd>
            <Link href="/my/follows" className={styles.statLink} aria-label={`팔로잉 ${formatCount(me.followingCount)}명 목록 보기`}>
              {formatCount(me.followingCount)}
            </Link>
          </dd>
        </div>
        <div className={styles.chip}>
          <dt>
            <ChipIcon size={12} /> 보유 칩
          </dt>
          <dd>
            <Link href="/my/coins" className={styles.statLink} aria-label={`보유 칩 ${formatCount(me.chipBalance)}개, 충전·사용 내역 보기`}>
              {formatCount(me.chipBalance)}
            </Link>
          </dd>
        </div>
      </dl>
      {/* 전에는 한 버튼이 글쓰기로만 가서 내 블로그를 볼 방법이 없었다. 둘로 나눈다. */}
      <div className={styles.writeRow}>
        <Link href={`/blog/${me.id}`} className={styles.action}>
          내 블로그
        </Link>
        <Link href="/blog/write" className={styles.write}>
          새 글 쓰기
          <ChevronRightIcon />
        </Link>
      </div>
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
