import Link from "next/link";
import type { MiniHomePost } from "@/app/_lib/types/miniHome";
import styles from "./Board.module.css";

type Props = {
  posts: MiniHomePost[];
};

export function RecentPosts({ posts }: Props) {
  return (
    <section className={styles.board} aria-labelledby="recent-posts-title">
      <div className={styles.header}>
        <h2 id="recent-posts-title" className={styles.title}>
          <span aria-hidden="true">📝</span> 최근 블로그 글
        </h2>
        <span className={styles.aside}>dev-log</span>
      </div>
      {posts.length === 0 ? (
        <p className={styles.empty}>아직 작성한 글이 없어요.</p>
      ) : (
        <ul className={styles.list}>
          {posts.map((post) => (
            <li key={post.id}>
              <Link href={`/blog/posts/${post.id}`} className={styles.row}>
                <span className={styles.text}>{post.title}</span>
                {post.membership ? (
                  <span className={styles.member}>
                    <span aria-hidden="true">🔒</span> 멤버십
                  </span>
                ) : (
                  <span className={styles.free}>무료</span>
                )}
              </Link>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
