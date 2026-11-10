"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import styles from "./LeftNav.module.css";

type SidePost = {
  id: number;
  title: string;
  score: number;
};

type SideCategory = {
  value: string;
  label: string;
};

type Props = {
  posts: SidePost[];
  categories: SideCategory[];
};

/** 피드 화면은 인기 블로그 글, 뉴스 화면은 카테고리 바로가기를 보여 준다. */
export function FeedSideCard({ posts, categories }: Props) {
  const pathname = usePathname();
  const news = pathname === "/news" || pathname.startsWith("/news/");

  if (news) {
    if (categories.length === 0) return null;
    return (
      <section className={styles.card} aria-labelledby="news-category-title">
        <h2 id="news-category-title" className={styles.title}>
          카테고리
        </h2>
        <ul className={styles.list}>
          {categories.map((category) => (
            <li key={category.value}>
              <Link href={`/news?category=${category.value}`} className={styles.item}>
                <span className={styles.name}>{category.label}</span>
              </Link>
            </li>
          ))}
        </ul>
      </section>
    );
  }

  if (posts.length === 0) return null;
  return (
    <section className={styles.card} aria-labelledby="popular-posts-title">
      <div className={styles.cardHeader}>
        <h2 id="popular-posts-title" className={styles.title}>
          인기 블로그 글
        </h2>
        <Link href="/blog" className={styles.more}>
          더 보기 →
        </Link>
      </div>
      <ol className={styles.list}>
        {posts.slice(0, 5).map((post, index) => (
          <li key={post.id}>
            <Link href={`/blog/posts/${post.id}`} className={styles.rankItem}>
              <span className={styles.rank}>{index + 1}</span>
              <span className={styles.name}>{post.title}</span>
              <span className={styles.score}>♥ {post.score}</span>
            </Link>
          </li>
        ))}
      </ol>
    </section>
  );
}
