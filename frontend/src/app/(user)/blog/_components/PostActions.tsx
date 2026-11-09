"use client";

import Link from "next/link";
import { useState, useTransition } from "react";
import {
  BookmarkIcon,
  CommentIcon,
  HeartIcon,
  RepostIcon,
} from "@/app/_components/ui/Icons";
import kit from "@/app/_components/ui/kit.module.css";
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
  loggedIn?: boolean;
};

export function PostActions({
  postId,
  likeCount,
  commentCount,
  repostCount,
  liked,
  bookmarked: initialBookmarked = false,
  readMinutes,
  loggedIn = false,
}: Props) {
  const [likeState, setLikeState] = useState({ active: liked, count: likeCount });
  const [error, setError] = useState<string | null>(null);
  const [bookmarked, setBookmarked] = useState(initialBookmarked);
  const [likePending, startLikeTransition] = useTransition();
  const [, startBookmarkTransition] = useTransition();

  function handleLike() {
    if (!loggedIn) {
      setError("로그인 후 좋아요를 누를 수 있어요.");
      return;
    }
    const previous = likeState;
    const next = !previous.active;
    setLikeState({ active: next, count: Math.max(0, previous.count + (next ? 1 : -1)) });
    setError(null);
    startLikeTransition(async () => {
      const result = await togglePostLike(postId, next);
      if (!result.ok) {
        setLikeState(previous);
        setError(result.message);
      }
    });
  }

  function handleBookmark() {
    if (!loggedIn) {
      setError("로그인 후 북마크할 수 있어요.");
      return;
    }
    const next = !bookmarked;
    setBookmarked(next);
    setError(null);
    startBookmarkTransition(async () => {
      const result = await togglePostBookmark(postId, next);
      if (!result.ok) {
        setBookmarked(!next);
        setError(result.message);
      }
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
      {error && (
        <p role="alert" className={kit.error} style={{ width: "100%" }}>
          {error}
        </p>
      )}
    </div>
  );
}
