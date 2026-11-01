"use client";

import Link from "next/link";
import { useState } from "react";
import {
  BookmarkIcon,
  CommentIcon,
  HeartIcon,
  RepostIcon,
} from "@/app/_components/ui/Icons";
import styles from "./PostActions.module.css";

type Props = {
  postId: number;
  likeCount: number;
  commentCount: number;
  repostCount: number;
  liked: boolean;
  readMinutes: number;
};

// 글 좋아요·북마크 API가 정해지기 전까지는 화면 상태만 바꾼다 (프론트엔드-청사진 11장).
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
        onClick={() => setBookmarked((value) => !value)}
      >
        <BookmarkIcon filled={bookmarked} />
      </button>
    </div>
  );
}
