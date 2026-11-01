"use client";

import Link from "next/link";
import { useState, useTransition } from "react";
import {
  BookmarkIcon,
  CommentIcon,
  HeartIcon,
  RepostIcon,
} from "@/app/_components/ui/Icons";
import { togglePostBookmark } from "../_actions";
import styles from "./PostActions.module.css";

type Props = {
  postId: number;
  likeCount: number;
  commentCount: number;
  repostCount: number;
  liked: boolean;
  readMinutes: number;
};

// 글 좋아요 API는 아직 없어 화면 상태만 바꾼다. 북마크는 posts/{postId}/bookmark로 보낸다.
export function PostActions({
  postId,
  likeCount,
  commentCount,
  repostCount,
  liked,
  readMinutes,
}: Props) {
  const [like, setLike] = useState(liked);
  const [bookmarked, setBookmarked] = useState(false);
  const [, startTransition] = useTransition();

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
        onClick={() => setLike((value) => !value)}
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
