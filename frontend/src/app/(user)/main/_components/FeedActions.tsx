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
  const error = like.error ?? repost.error;

  return (
    <div className={styles.actions}>
      <button
        type="button"
        className={`${styles.action} ${like.active ? styles.liked : ""}`}
        aria-pressed={like.active}
        disabled={like.pending}
        onClick={like.toggle}
      >
        <HeartIcon filled={like.active} />
        <span className="sr-only">좋아요</span>
        {like.count}
      </button>
      <Link href={`/feeds/${feedId}`} className={styles.action}>
        <CommentIcon />
        댓글 {commentCount}
      </Link>
      <button
        type="button"
        className={`${styles.action} ${repost.active ? styles.reposted : ""}`}
        aria-pressed={repost.active}
        disabled={repost.pending}
        onClick={repost.toggle}
      >
        <RepostIcon />
        리포스트 {repost.count}
      </button>
      <button
        type="button"
        className={`${styles.action} ${styles.bookmark}`}
        aria-pressed={bookmarked}
        aria-label="북마크"
        onClick={() => setBookmarked((value) => !value)}
      >
        <BookmarkIcon filled={bookmarked} />
      </button>
      <p role="alert" className={styles.error}>
        {error}
      </p>
    </div>
  );
}
