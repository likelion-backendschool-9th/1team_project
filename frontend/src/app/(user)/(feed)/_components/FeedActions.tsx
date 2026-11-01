"use client";

import Link from "next/link";
import { useState } from "react";
import {
  BookmarkIcon,
  CommentIcon,
  HeartIcon,
  RepostIcon,
} from "@/app/_components/ui/Icons";
import { toggleLike, toggleRepost } from "../_actions";
import { useToggle } from "../_hooks/useToggle";
import styles from "./FeedActions.module.css";

type Props = {
  feedId: number;
  likeCount: number;
  commentCount: number;
  repostCount: number;
  liked: boolean;
  reposted: boolean;
};

type Effect = "like" | "repost" | "bookmark";

export function FeedActions({
  feedId,
  likeCount,
  commentCount,
  repostCount,
  liked,
  reposted,
}: Props) {
  const like = useToggle(liked, likeCount, (next) => toggleLike(feedId, next));
  const repost = useToggle(reposted, repostCount, (next) =>
    toggleRepost(feedId, next),
  );
  const [bookmarked, setBookmarked] = useState(false);
  // 누를 때마다 값이 바뀌어 아이콘을 다시 그리고, 애니메이션을 처음부터 재생한다.
  const [plays, setPlays] = useState<Record<Effect, number>>({
    like: 0,
    repost: 0,
    bookmark: 0,
  });
  const error = like.error ?? repost.error;

  function play(effect: Effect) {
    setPlays((value) => ({ ...value, [effect]: value[effect] + 1 }));
  }

  function animation(effect: Effect, className: string) {
    return plays[effect] > 0 ? className : "";
  }

  return (
    <div className={styles.actions}>
      <button
        type="button"
        className={`${styles.action} ${styles.like} ${like.active ? styles.liked : ""}`}
        aria-pressed={like.active}
        disabled={like.pending}
        onClick={() => {
          play("like");
          like.toggle();
        }}
      >
        <span
          key={plays.like}
          className={`${styles.icon} ${animation("like", like.active ? styles.pop : styles.press)}`}
        >
          <HeartIcon filled={like.active} />
        </span>
        <span className="sr-only">좋아요</span>
        {like.count}
      </button>
      <Link href={`/feeds/${feedId}`} className={`${styles.action} ${styles.comment}`}>
        <span className={styles.icon}>
          <CommentIcon />
        </span>
        댓글 {commentCount}
      </Link>
      <button
        type="button"
        className={`${styles.action} ${styles.repost} ${repost.active ? styles.reposted : ""}`}
        aria-pressed={repost.active}
        disabled={repost.pending}
        onClick={() => {
          play("repost");
          repost.toggle();
        }}
      >
        <span key={plays.repost} className={`${styles.icon} ${animation("repost", styles.spin)}`}>
          <RepostIcon />
        </span>
        리포스트 {repost.count}
      </button>
      <button
        type="button"
        className={`${styles.action} ${styles.bookmark} ${bookmarked ? styles.saved : ""}`}
        aria-pressed={bookmarked}
        aria-label="북마크"
        onClick={() => {
          play("bookmark");
          setBookmarked((value) => !value);
        }}
      >
        <span
          key={plays.bookmark}
          className={`${styles.icon} ${animation("bookmark", bookmarked ? styles.drop : styles.press)}`}
        >
          <BookmarkIcon filled={bookmarked} />
        </span>
      </button>
      <p role="alert" className={styles.error}>
        {error}
      </p>
    </div>
  );
}
