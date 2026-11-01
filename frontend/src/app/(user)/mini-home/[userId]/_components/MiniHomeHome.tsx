import { notFound } from "next/navigation";
import { connection } from "next/server";
import {
  getComments,
  getDemos,
  getProfile,
  getRecentPosts,
  getServerTime,
} from "../_lib/api";
import { parseUserId } from "../_lib/parse";
import { FeaturedDemo } from "./FeaturedDemo";
import { Guestbook } from "./Guestbook";
import { PublicDemos } from "./PublicDemos";
import { RecentPosts } from "./RecentPosts";
import styles from "./MiniHomeFrame.module.css";

type Props = {
  params: Promise<{ userId: string }>;
};

export async function MiniHomeHome({ params }: Props) {
  await connection();
  const userId = parseUserId((await params).userId);
  const profile = userId ? await getProfile(userId) : null;
  if (!userId || !profile) notFound();

  const [demos, posts, comments, now] = await Promise.all([
    getDemos(userId),
    getRecentPosts(userId),
    getComments(userId),
    getServerTime(),
  ]);
  // 대표 데모는 공개된 데모 중 하나다. 지정이 없으면 첫 번째 데모를 쓴다.
  const featuredIndex = Math.max(
    0,
    demos.findIndex((demo) => demo.id === profile.featuredDemoId),
  );
  const featured = demos[featuredIndex];

  return (
    <>
      <FeaturedDemo demo={featured} variant={featuredIndex} postId={profile.featuredPostId} />
      <PublicDemos userId={userId} demos={demos} featuredId={featured?.id} />
      <div className={styles.boards}>
        <RecentPosts posts={posts} />
        <Guestbook
          userId={userId}
          comments={comments.items.slice(0, 3)}
          total={comments.total}
          now={now}
        />
      </div>
    </>
  );
}
