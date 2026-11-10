"use client";

import { useEffect, useId, useRef, useState, useTransition, type ReactNode } from "react";
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
  const wrapRef = useRef<HTMLDivElement>(null);
  const toggleRef = useRef<HTMLButtonElement>(null);

  useEffect(() => {
    if (!open) return;
    function handlePointer(event: PointerEvent) {
      const target = event.target as Element;
      // 댓글 삭제 확인 대화상자는 패널 밖(top layer)에 뜨므로 닫지 않는다.
      if (!wrapRef.current?.contains(target) && !target.closest("dialog")) setOpen(false);
    }
    function handleKey(event: KeyboardEvent) {
      if (event.key === "Escape" && !document.querySelector("dialog[open]")) {
        event.stopPropagation();
        setOpen(false);
        toggleRef.current?.focus();
      }
    }
    document.addEventListener("pointerdown", handlePointer);
    document.addEventListener("keydown", handleKey, true);
    return () => {
      document.removeEventListener("pointerdown", handlePointer);
      document.removeEventListener("keydown", handleKey, true);
    };
  }, [open]);

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
    <div ref={wrapRef} className={styles.social}>
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
          ref={toggleRef}
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
