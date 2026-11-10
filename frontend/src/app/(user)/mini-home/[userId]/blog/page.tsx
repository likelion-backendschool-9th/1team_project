import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { connection } from "next/server";
import { Suspense } from "react";
import kit from "@/app/_components/ui/kit.module.css";
import { getMe } from "@/app/(user)/_lib/api";
import { SeriesCard } from "@/app/(user)/blog/_components/SeriesCard";
import seriesStyles from "@/app/(user)/blog/_components/PopularSeries.module.css";
import { getUserSeries } from "@/app/(user)/blog/_lib/api";
import boardStyles from "../_components/Board.module.css";
import { PostLock } from "../_components/PostLock";
import { getMyPosts, getProfile } from "../_lib/api";
import { parseUserId } from "../_lib/parse";

export const metadata: Metadata = { title: "미니홈피 블로그" };

// 미니홈피 안에서 이 사람의 블로그 글 목록을 보여 준다. 글을 누르면 블로그 상세로 간다.
async function BlogContent({ params }: { params: Promise<{ userId: string }> }) {
  await connection();
  const userId = parseUserId((await params).userId);
  const profile = userId ? await getProfile(userId) : null;
  if (!userId || !profile) notFound();
  const [me, posts, seriesList] = await Promise.all([getMe(), getMyPosts(userId, 20), getUserSeries(userId)]);
  const isOwner = me?.id === userId;

  return (
    <div className={kit.stack}>
      {seriesList.length > 0 && (
        <section className={kit.card} aria-labelledby="mini-series-title">
          <h2 id="mini-series-title" className={kit.cardTitle}>
            <span aria-hidden="true">📚</span> 연재 시리즈
          </h2>
          <ul className={`${seriesStyles.grid} ${boardStyles.seriesList}`}>
            {seriesList.map((item) => (
              <li key={item.id}>
                <SeriesCard series={item} />
              </li>
            ))}
          </ul>
        </section>
      )}
    <section className={kit.card} aria-labelledby="mini-blog-title">
      <div className={kit.cardHeader}>
        <h2 id="mini-blog-title" className={kit.cardTitle}>
          <span aria-hidden="true">📝</span> {profile.nickname}님의 블로그
        </h2>
        <div className={kit.row}>
          {isOwner && (
            <Link href="/blog/write" className={`${kit.btnPrimary} ${kit.sm}`}>
              + 글 쓰기
            </Link>
          )}
          <Link href={`/blog/${userId}`} className={`${kit.btnGhost} ${kit.sm}`}>
            블로그에서 보기 →
          </Link>
        </div>
      </div>
      {posts.length === 0 ? (
        <p className={kit.empty}>아직 작성한 글이 없어요.</p>
      ) : (
        <ul className={boardStyles.list}>
          {posts.map((post) => (
            <li key={post.id}>
              <Link href={`/blog/posts/${post.id}`} className={boardStyles.row}>
                <span className={boardStyles.text}>{post.title}</span>
                <PostLock post={post} />
              </Link>
            </li>
          ))}
        </ul>
      )}
    </section>
    </div>
  );
}

export default function MiniHomeBlogPage({ params }: PageProps<"/mini-home/[userId]/blog">) {
  return (
    <>
      <h1 className="sr-only">미니홈피 블로그</h1>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <BlogContent params={params} />
      </Suspense>
    </>
  );
}
