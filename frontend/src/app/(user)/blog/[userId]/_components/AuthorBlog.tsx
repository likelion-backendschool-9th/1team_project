import Link from "next/link";
import { notFound } from "next/navigation";
import { connection } from "next/server";
import { Avatar } from "@/app/_components/ui/Avatar";
import kit from "@/app/_components/ui/kit.module.css";
import { formatCount } from "@/app/_lib/format";
import { getMe } from "@/app/(user)/_lib/api";
import { createSeries } from "../../_actions";
import { ArticleCard } from "../../_components/ArticleCard";
import { SeriesCard } from "../../_components/SeriesCard";
import { SeriesForm } from "../../_components/SeriesForm";
import series from "../../_components/PopularSeries.module.css";
import { getBlogOwner, getServerTime, getUserPosts, getUserSeries } from "../../_lib/api";
import styles from "./AuthorBlog.module.css";

type Props = {
  params: Promise<{ userId: string }>;
};

export async function AuthorBlog({ params }: Props) {
  await connection();
  const userId = Number((await params).userId);
  const owner = Number.isSafeInteger(userId) && userId > 0 ? await getBlogOwner(userId) : null;
  if (!owner) notFound();

  const [me, posts, seriesList, now] = await Promise.all([
    getMe(),
    getUserPosts(userId),
    getUserSeries(userId),
    getServerTime(),
  ]);
  const mine = me?.id === userId;

  return (
    <div className={kit.stack}>
      <section className={`${kit.card} ${styles.hero}`} aria-labelledby="blog-owner">
        <Avatar name={owner.user.nickname} size={80} />
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
              <dt>시리즈</dt>
              <dd>{formatCount(seriesList.length)}</dd>
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
          ) : null}
          <Link href={`/mini-home/${userId}`} className={kit.btnGhost}>
            미니홈피
          </Link>
        </div>
      </section>

      {(seriesList.length > 0 || mine) && (
        <section aria-labelledby="owner-series">
          <h2 id="owner-series" className={kit.cardTitle} style={{ marginBottom: 12 }}>
            연재 시리즈 {seriesList.length}
          </h2>
          {seriesList.length === 0 ? (
            <p className={`${kit.card} ${kit.empty}`}>
              아직 시리즈가 없어요. 시리즈를 만들고 글쓰기에서 고르면 회차로 붙어요.
            </p>
          ) : (
            <ul className={series.grid}>
              {seriesList.map((item) => (
                <li key={item.id}>
                  <SeriesCard series={item} />
                </li>
              ))}
            </ul>
          )}
          {mine && (
            <details className={`${kit.card} ${styles.newSeries}`}>
              <summary className={kit.strong}>+ 새 시리즈 만들기</summary>
              <SeriesForm action={createSeries} submitLabel="시리즈 만들기" />
            </details>
          )}
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
