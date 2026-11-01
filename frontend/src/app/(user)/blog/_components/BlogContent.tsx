import Link from "next/link";
import { connection } from "next/server";
import { getPopularSeries, getPosts, getServerTime } from "../_lib/api";
import { blogHref, parseCategory, parseKeyword, parseSort, sorts } from "../_lib/query";
import { ArticleCard } from "./ArticleCard";
import { BlogSearch } from "./BlogSearch";
import { PopularSeries } from "./PopularSeries";
import styles from "./BlogContent.module.css";

type Props = {
  searchParams: Promise<Record<string, string | string[] | undefined>>;
};

export async function BlogContent({ searchParams }: Props) {
  await connection();
  const params = await searchParams;
  const query = {
    category: parseCategory(params.category),
    sort: parseSort(params.sort),
    q: parseKeyword(params.q),
  };
  const [series, posts, now] = await Promise.all([
    getPopularSeries(),
    getPosts(query),
    getServerTime(),
  ]);

  return (
    <>
      <BlogSearch {...query} />
      <PopularSeries series={series} />
      <section aria-labelledby="latest-title">
        <div className={styles.header}>
          <h2 id="latest-title" className={styles.title}>
            최신 기술 아티클
          </h2>
          <nav aria-label="아티클 정렬">
            <ul className={styles.sorts}>
              {sorts.map((sort) => (
                <li key={sort.value}>
                  <Link
                    href={blogHref({ ...query, sort: sort.value })}
                    className={`${styles.sort} ${query.sort === sort.value ? styles.activeSort : ""}`}
                    aria-current={query.sort === sort.value ? "page" : undefined}
                  >
                    {sort.label}
                  </Link>
                </li>
              ))}
            </ul>
          </nav>
        </div>
        {posts.length === 0 ? (
          <p className={styles.empty}>조건에 맞는 아티클이 없어요.</p>
        ) : (
          <ul className={styles.list}>
            {posts.map((post) => (
              <li key={post.id}>
                <ArticleCard post={post} now={now} />
              </li>
            ))}
          </ul>
        )}
      </section>
    </>
  );
}
