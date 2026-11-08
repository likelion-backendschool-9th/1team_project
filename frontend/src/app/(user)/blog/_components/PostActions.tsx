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
  /** 목록 카드에서는 알 수 없어 생략한다 (기본 false) */
  bookmarked?: boolean;
  readMinutes: number;
};

// 좋아요는 posts/{postId}/like, 북마크는 posts/{postId}/bookmark로 보낸다. 실패하면 되돌린다.
export function PostActions({
  postId,
  likeCount,
  commentCount,
  repostCount,
  liked,
  bookmarked: initialBookmarked = false,
  readMinutes,
}: Props) {
  const [like, setLike] = useState(liked);
  const [bookmarked, setBookmarked] = useState(initialBookmarked);
  const [, startTransition] = useTransition();

  function handleLike() {
    const next = !like;
    setLike(next);
    startTransition(async () => {
      const result = await togglePostLike(postId, next);
      if (!result.ok) setLike(!next);
    });
  }

  function handleBookmark() {
    const next = !bookmarked;
    setBookmarked(next);
    startTransition(async () => {
      const result = await togglePostBookmark(postId, next);
      if (!result.ok) setBookmarked(!next);
    });
  }

  return (
    <div className={styles.actions}>
      <button
        type="button"
        className={`${styles.action} ${like ? styles.liked : ""}`}
        aria-pressed={like}
        onClick={handleLike}
      >
        <HeartIcon filled={like} />
        <span className="sr-only">좋아요</span>
        {likeCount - (liked ? 1 : 0) + (like ? 1 : 0)}
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
