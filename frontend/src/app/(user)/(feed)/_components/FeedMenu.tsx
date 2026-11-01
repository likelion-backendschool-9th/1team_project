"use client";

import Link from "next/link";
import { useEffect, useId, useRef, useState } from "react";
import { ConfirmButton } from "@/app/_components/ui/ConfirmButton";
import { MoreIcon } from "@/app/_components/ui/Icons";
import { blockUser, deleteFeed, reportFeed } from "../_actions";
import styles from "./FeedMenu.module.css";

type Props = {
  feedId: number;
  authorId: number;
  authorName: string;
  mine: boolean;
};

// 피드 카드 ⋮ 메뉴: 내 글이면 수정·삭제, 남의 글이면 신고·차단
export function FeedMenu({ feedId, authorId, authorName, mine }: Props) {
  const [open, setOpen] = useState(false);
  const [message, setMessage] = useState<{ ok: boolean; message: string } | null>(null);
  const menuId = useId();
  const wrapRef = useRef<HTMLDivElement>(null);
  const buttonRef = useRef<HTMLButtonElement>(null);

  useEffect(() => {
    if (!open) return;
    function handlePointer(event: PointerEvent) {
      const target = event.target as Element;
      // 확인 대화상자는 메뉴 밖(top layer)에 뜨므로 닫지 않는다.
      if (!wrapRef.current?.contains(target) && !target.closest("dialog")) setOpen(false);
    }
    function handleKey(event: KeyboardEvent) {
      if (event.key === "Escape" && !document.querySelector("dialog[open]")) {
        setOpen(false);
        buttonRef.current?.focus();
      }
    }
    document.addEventListener("pointerdown", handlePointer);
    document.addEventListener("keydown", handleKey);
    return () => {
      document.removeEventListener("pointerdown", handlePointer);
      document.removeEventListener("keydown", handleKey);
    };
  }, [open]);

  useEffect(() => {
    if (!message) return;
    const timer = window.setTimeout(() => setMessage(null), 3500);
    return () => window.clearTimeout(timer);
  }, [message]);

  function done(result: { ok: boolean; message: string }) {
    setMessage(result);
    setOpen(false);
  }

  return (
    <div ref={wrapRef} className={styles.wrap}>
      <button
        ref={buttonRef}
        type="button"
        className={styles.trigger}
        aria-label="더보기"
        aria-expanded={open}
        aria-controls={menuId}
        onClick={() => setOpen((value) => !value)}
      >
        <MoreIcon />
      </button>
      <div id={menuId} className={styles.menu} hidden={!open}>
        {mine ? (
          <>
            <Link href={`/feeds/${feedId}?edit=1`} className={styles.item}>
              ✏️ 수정
            </Link>
            <ConfirmButton
              label="🗑️ 삭제"
              title="피드를 삭제할까요?"
              description="삭제한 피드와 댓글은 되돌릴 수 없어요."
              confirmLabel="삭제"
              danger
              className={`${styles.item} ${styles.danger}`}
              onConfirm={deleteFeed.bind(null, feedId)}
              onDone={done}
            />
          </>
        ) : (
          <>
            <ConfirmButton
              label="🚨 신고"
              title="이 피드를 신고할까요?"
              description="운영진이 확인 후 조치해요."
              confirmLabel="신고"
              danger
              className={styles.item}
              onConfirm={reportFeed.bind(null, feedId)}
              onDone={done}
            />
            <ConfirmButton
              label={`🚫 ${authorName}님 차단`}
              title={`${authorName}님을 차단할까요?`}
              description="차단하면 서로의 피드와 DM이 보이지 않아요. 설정에서 해제할 수 있어요."
              confirmLabel="차단"
              danger
              className={`${styles.item} ${styles.danger}`}
              onConfirm={blockUser.bind(null, authorId)}
              onDone={done}
            />
          </>
        )}
      </div>
      <p role="status" className={message?.ok === false ? styles.error : styles.status}>
        {message?.message}
      </p>
    </div>
  );
}
