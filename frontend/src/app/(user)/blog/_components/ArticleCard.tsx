import Link from "next/link";
import { Avatar } from "@/app/_components/ui/Avatar";
import { formatRelativeTime, formatWon } from "@/app/_lib/format";
import { chipsToWon } from "@/app/_lib/policy";
import type { PostSummary } from "@/app/_lib/types/post";
import { isPaidPost } from "../_lib/query";
import { PostActions } from "./PostActions";
import { PostDemo } from "./PostDemo";
import styles from "./ArticleCard.module.css";

type Props = {
  post: PostSummary;
  now: number;
  loggedIn?: boolean;
};

export function ArticleCard({ post, now, loggedIn }: Props) {
  const { author, demo } = post;
  const paid = isPaidPost(post);
  // 시리즈 안 유료 글은 칩 가격 없이 시리즈를 사야 읽는다.
  const inSeries = paid && Boolean(post.seriesId);

  return (
    <article className={styles.card} aria-labelledby={`post-${post.id}`}>
      <header className={styles.header}>
        <Link href={`/blog/${author.id}`} aria-label={`${author.nickname} 블로그`}>
          <Avatar name={author.nickname} src={author.profileImage} size={40} />
        </Link>
        <p className={styles.meta}>
          <Link href={`/blog/${author.id}`} className={styles.name}>
            {author.nickname}
          </Link>
          <span>
            {" "}
            · {author.techStacks[0]} ·{" "}
            <time dateTime={post.createdAt}>{formatRelativeTime(post.createdAt, now)}</time>
          </span>
        </p>
        {!paid ? (
          <span className={styles.free}>무료 공개</span>
        ) : inSeries ? (
          <span className={styles.locked}>
            <span aria-hidden="true">🔒</span> 시리즈 구매자 전용
          </span>
        ) : (
          <span className={styles.locked}>
            <span aria-hidden="true">🔒</span> {post.price} 칩 잠금해제
          </span>
        )}
      </header>

      <h3 id={`post-${post.id}`} className={styles.title}>
        <Link href={`/blog/posts/${post.id}`} className={styles.titleLink}>
          {post.title}
        </Link>
      </h3>
      {/* 요약이 제목으로 시작하면(본문 첫 줄이 제목) 제목을 빼고 보여 준다 */}
      <p className={styles.excerpt}>
        {post.excerpt.startsWith(post.title) ? post.excerpt.slice(post.title.length).trimStart() : post.excerpt}
      </p>
      {/* 유료 글은 글 상세의 잠금 안내처럼 미리보기에서도 이어지는 내용이 잠겨 있다고 보여 준다 */}
      {paid && (
        <Link href={`/blog/posts/${post.id}`} className={styles.paywall}>
          <span className={styles.paywallLock} aria-hidden="true">
            🔒
          </span>
          {inSeries
            ? "이어지는 내용은 시리즈를 사면 읽을 수 있어요"
            : `이어지는 내용은 ${post.price}칩(${formatWon(chipsToWon(post.price))})으로 열람할 수 있어요`}
        </Link>
      )}

      {demo && <PostDemo demo={demo} />}

      <PostActions
        postId={post.id}
        likeCount={post.likeCount}
        commentCount={post.commentCount}
        liked={post.liked}
        readMinutes={post.readMinutes}
        loggedIn={loggedIn}
      />
    </article>
  );
}
