import Link from "next/link";
import { Avatar } from "@/app/_components/ui/Avatar";
import { ArrowRightIcon, MoreIcon } from "@/app/_components/ui/Icons";
import { formatRelativeTime, formatWon } from "@/app/_lib/format";
import { chipsToWon } from "@/app/_lib/policy";
import type { PostSummary } from "@/app/_lib/types/post";
import { PostActions } from "./PostActions";
import styles from "./ArticleCard.module.css";

type Props = {
  post: PostSummary;
  now: number;
  loggedIn?: boolean;
};

export function ArticleCard({ post, now, loggedIn }: Props) {
  const { author, demo } = post;

  return (
    <article className={styles.card} aria-labelledby={`post-${post.id}`}>
      <header className={styles.header}>
        <Link href={`/blog/${author.id}`} aria-label={`${author.nickname} 블로그`}>
          <Avatar name={author.nickname} size={40} />
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
        {post.price > 0 ? (
          <span className={styles.locked}>
            <span aria-hidden="true">🔒</span> {post.price} 칩 잠금해제
          </span>
        ) : (
          <span className={styles.free}>무료 공개</span>
        )}
        {post.price > 0 && (
          <button type="button" className={styles.more} aria-label="더보기" disabled>
            <MoreIcon />
          </button>
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
      {post.price > 0 && (
        <Link href={`/blog/posts/${post.id}`} className={styles.paywall}>
          <span className={styles.paywallLock} aria-hidden="true">
            🔒
          </span>
          이어지는 내용은 {post.price}칩({formatWon(chipsToWon(post.price))})으로 열람할 수 있어요
        </Link>
      )}

      {demo && (
        <div className={styles.source}>
          <span className={styles.code} aria-hidden="true">
            &lt;/&gt;
          </span>
          <div className={styles.sourceInfo}>
            <p className={styles.sourceTitle}>{demo.title}</p>
            <p className={styles.sourceDesc}>{demo.description}</p>
          </div>
          <Link href={`/demos/${demo.id}`} className={styles.play}>
            소스 찍먹하기
            <ArrowRightIcon />
          </Link>
        </div>
      )}

      <PostActions
        postId={post.id}
        likeCount={post.likeCount}
        commentCount={post.commentCount}
        repostCount={post.repostCount}
        liked={post.liked}
        readMinutes={post.readMinutes}
        loggedIn={loggedIn}
      />
    </article>
  );
}
