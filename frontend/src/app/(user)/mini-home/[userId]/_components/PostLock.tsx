import { isPaidPost } from "@/app/(user)/blog/_lib/query";
import type { MiniHomePost } from "@/app/_lib/types/miniHome";
import styles from "./Board.module.css";

/** 미니홈피 글 목록의 잠금 표시. 무료 / 시리즈 / 칩 */
export function PostLock({ post }: { post: MiniHomePost }) {
  if (!isPaidPost(post)) return <span className={styles.free}>무료</span>;
  if (post.seriesId) {
    return (
      <span className={styles.member}>
        <span aria-hidden="true">🔒</span> 시리즈
      </span>
    );
  }
  return (
    <span className={styles.member}>
      <span aria-hidden="true">🔒</span> {post.price}칩
    </span>
  );
}
