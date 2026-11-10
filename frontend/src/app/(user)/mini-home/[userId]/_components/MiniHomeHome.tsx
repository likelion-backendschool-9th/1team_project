import { notFound } from "next/navigation";
import { connection } from "next/server";
import { getMe } from "@/app/(user)/_lib/api";
import {
  getRecentGuestbook,
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
    getRecentGuestbook(userId),
    getServerTime(),
  ]);
  // 미니홈피에는 고른 데모만 보여 준다 (일반 3개, 딥소스 플러스 6개).
  // 아직 하나도 고르지 않았으면 빈 화면 대신 최근 데모를 보여 준다. 고르면 그 데모만 보인다.
  const chosen = profile.showcaseDemoIds.length > 0;
  const demos = (chosen ? inventory.filter((demo) => profile.showcaseDemoIds.includes(demo.id)) : inventory)
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
        me={me!}
        now={now}
        isOwner={me?.id === userId}
      />
      {/* 데모가 없으면 위 대표 데모 칸에서 안내하므로, 같은 문구를 두 번 보여 주지 않는다 */}
      {demos.length > 0 && <PublicDemos userId={userId} demos={demos} featuredId={featured?.id} />}
      <div className={styles.boards}>
        <RecentPosts posts={posts} />
        <Guestbook
          userId={userId}
          comments={comments.items.slice(0, 3)}
          total={comments.total}
          now={now}
          me={me ?? undefined}
        />
      </div>
    </>
  );
}
