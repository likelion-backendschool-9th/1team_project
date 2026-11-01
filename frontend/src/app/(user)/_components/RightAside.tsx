import Link from "next/link";
import { Suspense } from "react";
import { GearIcon } from "@/app/_components/ui/Icons";
import { getFollowings, getMe, getUnreadCount } from "../_lib/api";
import { FollowingList } from "./FollowingList";
import { ProfileCard } from "./ProfileCard";
import styles from "./RightAside.module.css";

async function RightAsideContent() {
  const [me, followings, unreadCount] = await Promise.all([
    getMe(),
    getFollowings(),
    getUnreadCount(),
  ]);

  return (
    <>
      <ProfileCard me={me} unreadCount={unreadCount} />
      <FollowingList followings={followings} />
    </>
  );
}

export function RightAside() {
  return (
    <aside className={styles.aside} aria-label="내 정보와 팔로잉">
      <Suspense fallback={<p className={styles.loading}>불러오는 중…</p>}>
        <RightAsideContent />
      </Suspense>
      <div className={styles.footer}>
        <Link href="/my/settings" className={styles.settings}>
          <GearIcon className={styles.settingsIcon} />
          설정
        </Link>
      </div>
    </aside>
  );
}
