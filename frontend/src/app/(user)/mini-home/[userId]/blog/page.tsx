import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { connection } from "next/server";
import { Suspense } from "react";
import kit from "@/app/_components/ui/kit.module.css";
import { getMe } from "@/app/(user)/_lib/api";
import boardStyles from "../_components/Board.module.css";
import { getMyPosts, getProfile } from "../_lib/api";
import { parseUserId } from "../_lib/parse";

export const metadata: Metadata = { title: "미니홈피 블로그" };

// 미니홈피 안에서 이 사람의 블로그 글 목록을 보여 준다. 글을 누르면 블로그 상세로 간다.
async function BlogContent({ params }: { params: Promise<{ userId: string }> }) {
  await connection();
  const userId = parseUserId((await params).userId);
  const profile = userId ? await getProfile(userId) : null;
  if (!userId || !profile) notFound();
  const [me, posts] = await Promise.all([getMe(), getMyPosts(userId, 20)]);
  const isOwner = me?.id === userId;

  return (
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
                {post.membership ? (
                  <span className={boardStyles.member}>
                    <span aria-hidden="true">🔒</span> 멤버십
                  </span>
                ) : (
                  <span className={boardStyles.free}>무료</span>
                )}
              </Link>
            </li>
          ))}
        </ul>
      )}
    </section>
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
