"use client";

import Link from "next/link";
import { useState, useTransition } from "react";
import {
  BookmarkIcon,
  CommentIcon,
  HeartIcon,
  RepostIcon,
} from "@/app/_components/ui/Icons";
import { togglePostBookmark, togglePostLike } from "../_actions";
import styles from "./PostActions.module.css";

type Props = {
  postId: number;
  likeCount: number;
  commentCount: number;
  repostCount: number;
  liked: boolean;
  readMinutes: number;
};

export function PostActions({
  postId,
  likeCount,
  commentCount,
  repostCount,
  liked,
  readMinutes,
}: Props) {
  const [likeState, setLikeState] = useState({ active: liked, count: likeCount });
  const [bookmarked, setBookmarked] = useState(false);
  const [likePending, startLikeTransition] = useTransition();
  const [, startBookmarkTransition] = useTransition();

  function handleLike() {
    const previous = likeState;
    const next = !previous.active;
    setLikeState({ active: next, count: previous.count + (next ? 1 : -1) });
    startLikeTransition(async () => {
      const result = await togglePostLike(postId, next);
      if (!result.ok) setLikeState(previous);
    });
  }

  function handleBookmark() {
    const next = !bookmarked;
    setBookmarked(next);
    startBookmarkTransition(async () => {
      const result = await togglePostBookmark(postId, next);
      if (!result.ok) setBookmarked(!next);
    });
  }

  return (
    <div className={styles.actions}>
      <button
        type="button"
        className={`${styles.action} ${likeState.active ? styles.liked : ""}`}
        aria-pressed={likeState.active}
        disabled={likePending}
        onClick={handleLike}
      >
        <HeartIcon filled={likeState.active} />
        <span className="sr-only">좋아요</span>
        {likeState.count}
      </button>
      <Link href={`/blog/posts/${postId}#comments`} className={styles.action}>
        <CommentIcon />
        댓글 {commentCount}
      </Link>
      <span className={styles.action}>
        <RepostIcon />
        리포스트 {repostCount}
      </span>
      <span className={styles.read}>읽는 시간 {readMinutes}분</span>
      <button
        type="button"
        className={styles.action}
        aria-pressed={bookmarked}
        aria-label="북마크"
        onClick={handleBookmark}
      >
        <BookmarkIcon filled={bookmarked} />
      </button>
    </div>
  );
}
