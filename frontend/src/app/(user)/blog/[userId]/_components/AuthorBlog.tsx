import Link from "next/link";
import { notFound } from "next/navigation";
import { connection } from "next/server";
import { Avatar } from "@/app/_components/ui/Avatar";
import { ConfirmButton } from "@/app/_components/ui/ConfirmButton";
import kit from "@/app/_components/ui/kit.module.css";
import { formatCount, formatWon } from "@/app/_lib/format";
import { getMe } from "@/app/(user)/_lib/api";
import { subscribeBlog } from "../../_actions";
import { ArticleCard } from "../../_components/ArticleCard";
import { getBlogOwner, getPopularSeries, getServerTime, getUserPosts } from "../../_lib/api";
import styles from "./AuthorBlog.module.css";

type Props = {
  params: Promise<{ userId: string }>;
};

export async function AuthorBlog({ params }: Props) {
  await connection();
  const userId = Number((await params).userId);
  const owner = Number.isSafeInteger(userId) && userId > 0 ? await getBlogOwner(userId) : null;
  if (!owner) notFound();

  const [me, posts, allSeries, now] = await Promise.all([
    getMe(),
    getUserPosts(userId),
    getPopularSeries(),
    getServerTime(),
  ]);
  const series = allSeries.filter((item) => item.author.id === userId);
  const mine = me?.id === userId;

  return (
    <div className={kit.stack}>
      <section className={`${kit.card} ${styles.hero}`} aria-labelledby="blog-owner">
        <Avatar name={owner.user.nickname} size={80} shape="square" />
        <div className={kit.grow}>
          <h2 id="blog-owner" className={styles.name}>
            {owner.user.nickname}의 개발 블로그
          </h2>
          <p className={kit.muted}>{owner.bio}</p>
          <dl className={styles.stats}>
            <div>
              <dt>글</dt>
              <dd>{formatCount(posts.length)}</dd>
            </div>
            <div>
              <dt>구독자</dt>
              <dd>{formatCount(owner.subscriberCount)}</dd>
            </div>
            <div>
              <dt>총 조회</dt>
              <dd>{formatCount(owner.totalViews)}</dd>
            </div>
          </dl>
        </div>
        <div className={styles.actions}>
          {mine ? (
            <Link href="/blog/write" className={kit.btnPrimary}>
              새 글 쓰기
            </Link>
          ) : owner.subscribed ? (
            <span className={kit.pillSuccess}>✓ 구독 중</span>
          ) : owner.subscriptionPrice <= 0 ? null : (
            <ConfirmButton
              label={`구독하기 · 월 ${formatWon(owner.subscriptionPrice)}`}
              title={`${owner.user.nickname}님의 블로그를 구독할까요?`}
              description={`매달 ${formatWon(owner.subscriptionPrice)}이 자동이체돼요. 칩은 차감되지 않아요.`}
              confirmLabel="구독하기"
              className={kit.btnPrimary}
              onConfirm={subscribeBlog.bind(null, userId)}
            />
          )}
          <Link href={`/mini-home/${userId}`} className={kit.btnGhost}>
            미니홈피
          </Link>
        </div>
      </section>

      {series.length > 0 && (
        <section aria-labelledby="owner-series">
          <h2 id="owner-series" className={kit.cardTitle} style={{ marginBottom: 12 }}>
            연재 시리즈
          </h2>
          <ul className={kit.grid2}>
            {series.map((item) => (
              <li key={item.id} className={kit.card}>
                <span className={kit.pillWarn}>
                  {item.status === "completed" ? `총 ${item.episodeCount}화 완결` : `${item.schedule} 연재중`}
                </span>
                <h3 className={styles.seriesTitle}>{item.title}</h3>
                <p className={kit.faint}>{item.description}</p>
                <Link href={`/blog/posts/${item.firstPostId}`} className={kit.more} style={{ marginTop: 10 }}>
                  1화부터 읽기 →
                </Link>
              </li>
            ))}
          </ul>
        </section>
      )}

      <section aria-labelledby="owner-posts">
        <h2 id="owner-posts" className={kit.cardTitle} style={{ marginBottom: 12 }}>
          전체 글 {posts.length}
        </h2>
        {posts.length === 0 ? (
          <p className={`${kit.card} ${kit.empty}`}>아직 작성한 글이 없어요.</p>
        ) : (
          <ul className={kit.stack}>
            {posts.map((post) => (
              <li key={post.id}>
                <ArticleCard post={post} now={now} loggedIn={me !== null} />
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  );
}
