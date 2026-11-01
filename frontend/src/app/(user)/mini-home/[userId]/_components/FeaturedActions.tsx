"use client";

import { useId, useState, useTransition, type ReactNode } from "react";
import { CommentIcon, HeartIcon } from "@/app/_components/ui/Icons";
import { toggleDemoLike } from "../_actions";
import styles from "./FeaturedDemo.module.css";

type Props = {
  demoId: number;
  likeCount: number;
  commentCount: number;
  children: ReactNode;
};

// 피드 카드처럼 좋아요·댓글 버튼. 댓글을 누르면 아래로 댓글 목록과 입력창이 펼쳐진다.
export function FeaturedActions({ demoId, likeCount, commentCount, children }: Props) {
  const [like, setLike] = useState({ active: false, count: likeCount });
  const [pop, setPop] = useState(0);
  const [open, setOpen] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [pending, startTransition] = useTransition();
  const panelId = useId();

  function handleLike() {
    const previous = like;
    const next = { active: !like.active, count: like.count + (like.active ? -1 : 1) };
    setLike(next);
    setPop((value) => value + 1);
    setError(null);
    startTransition(async () => {
      const result = await toggleDemoLike(demoId, next.active);
      if (!result.ok) {
        setLike(previous);
        setError(result.message ?? "잠시 후 다시 시도해 주세요.");
      }
    });
  }

  return (
    <div className={styles.social}>
      <div className={styles.socialButtons}>
        <button
          type="button"
          className={`${styles.socialButton} ${styles.likeButton} ${like.active ? styles.liked : ""}`}
          aria-pressed={like.active}
          disabled={pending}
          onClick={handleLike}
        >
          <span key={pop} className={`${styles.socialIcon} ${pop > 0 && like.active ? styles.pop : ""}`}>
            <HeartIcon filled={like.active} />
          </span>
          <span className="sr-only">좋아요</span>
          {like.count}
        </button>
        <button
          type="button"
          className={`${styles.socialButton} ${styles.commentButton} ${open ? styles.commentOpen : ""}`}
          aria-expanded={open}
          aria-controls={panelId}
          onClick={() => setOpen((value) => !value)}
        >
          <span className={styles.socialIcon}>
            <CommentIcon />
          </span>
          댓글 {commentCount}
          <span className={styles.caret} aria-hidden="true">
            {open ? "▴" : "▾"}
          </span>
        </button>
      </div>
      <p role="alert" className={styles.socialError}>
        {error}
      </p>
      <div id={panelId} className={styles.comments} hidden={!open}>
        {children}
      </div>
    </div>
  );
}
