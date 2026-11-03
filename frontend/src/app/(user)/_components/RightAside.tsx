import { connection } from "next/server";
import { Suspense } from "react";
import {
  getFollowings,
  getMe,
  getNotifications,
  getServerTime,
  getUnreadCount,
} from "../_lib/api";
import { isLoggedIn } from "../_lib/session";
import { FollowingList } from "./FollowingList";
import { LoginPrompt } from "./LoginPrompt";
import { ProfileCard } from "./ProfileCard";
import styles from "./RightAside.module.css";

async function RightAsideContent() {
  await connection();
  if (!(await isLoggedIn())) return <LoginPrompt />;

  const [me, followings, unreadCount, notifications, now] = await Promise.all([
    getMe(),
    getFollowings(),
    getUnreadCount(),
    getNotifications(),
    getServerTime(),
  ]);

  return (
    <>
      <ProfileCard me={me} unreadCount={unreadCount} notifications={notifications} now={now} />
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
    </aside>
  );
}
