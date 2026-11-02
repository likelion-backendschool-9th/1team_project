import { notFound } from "next/navigation";
import { connection } from "next/server";
import { getMe } from "@/app/(user)/_lib/api";
import {
  getComments,
  getDemoComments,
  getDemos,
  getProfile,
  getRecentPosts,
  getServerTime,
} from "../_lib/api";
import { showcaseLimit } from "@/app/_lib/policy";
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

  const [inventory, posts, comments, now] = await Promise.all([
    getDemos(userId, 20),
    getRecentPosts(userId),
    getComments(userId),
    getServerTime(),
  ]);
  // 미니홈피에는 고른 데모만 보여 준다 (일반 3개, 딥소스 플러스 6개).
  const demos = inventory
    .filter((demo) => profile.showcaseDemoIds.includes(demo.id))
    .slice(0, showcaseLimit(profile.plus));
  // 대표 데모는 공개된 데모 중 하나다. 지정이 없으면 첫 번째 데모를 쓴다.
  const featuredIndex = Math.max(
    0,
    demos.findIndex((demo) => demo.id === profile.featuredDemoId),
  );
  const featured = demos[featuredIndex];
  const [me, featuredComments] = await Promise.all([
    getMe(),
    featured ? getDemoComments(featured.id) : Promise.resolve([]),
  ]);

  return (
    <>
      <FeaturedDemo
        demo={featured}
        variant={featuredIndex}
        postId={profile.featuredPostId}
        comments={featuredComments}
        me={me}
        now={now}
      />
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
