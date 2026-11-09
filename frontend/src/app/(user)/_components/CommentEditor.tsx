"use client";

import { useState, useTransition } from "react";
import { ConfirmButton } from "@/app/_components/ui/ConfirmButton";
import kit from "@/app/_components/ui/kit.module.css";
import styles from "./CommentList.module.css";

type Result = { ok: boolean; message: string };

type Props = {
  commentId: number;
  content: string;
  maxLength?: number;
  updateComment: (commentId: number, content: string) => Promise<Result>;
  deleteComment: (commentId: number) => Promise<Result>;
  onReply?: () => void;
};

// 내가 쓴 댓글의 본문 자리. 수정·삭제를 그 자리에서 처리한다.
export function CommentEditor({ commentId, content, maxLength = 300, updateComment, deleteComment, onReply }: Props) {
  const [text, setText] = useState(content);
  const [draft, setDraft] = useState(content);
  const [editing, setEditing] = useState(false);
  const [deleted, setDeleted] = useState(false);
  const [message, setMessage] = useState<Result | null>(null);
  const [pending, startTransition] = useTransition();

  function save() {
    startTransition(async () => {
      const result = await updateComment(commentId, draft);
      setMessage(result);
      if (result.ok) {
        setText(draft.trim());
        setEditing(false);
      }
    });
  }

  if (deleted) return <p className={styles.deleted}>삭제한 댓글이에요.</p>;

  return (
    <>
      {editing ? (
        <div className={styles.editBox}>
          <label htmlFor={`comment-edit-${commentId}`} className="sr-only">
            댓글 수정
          </label>
          <textarea
            id={`comment-edit-${commentId}`}
            value={draft}
            maxLength={maxLength}
            rows={2}
            className={kit.textarea}
            style={{ minHeight: 64 }}
            onChange={(event) => setDraft(event.target.value)}
          />
          <div className={kit.row}>
            <button type="button" className={`${kit.btnPrimary} ${kit.sm}`} disabled={pending} onClick={save}>
              저장
            </button>
            <button
              type="button"
              className={`${kit.btn} ${kit.sm}`}
              onClick={() => {
                setDraft(text);
                setEditing(false);
              }}
            >
              취소
            </button>
          </div>
        </div>
      ) : (
        <p className={styles.content}>{text}</p>
      )}
      {!editing && (
        <div className={styles.mine}>
          {onReply && (
            <button type="button" className={styles.mineButton} onClick={onReply}>
              답글
            </button>
          )}
          <button type="button" className={styles.mineButton} onClick={() => setEditing(true)}>
            수정
          </button>
          <ConfirmButton
            label="삭제"
            title="댓글을 삭제할까요?"
            confirmLabel="삭제"
            danger
            className={styles.mineButton}
            onConfirm={() => deleteComment(commentId)}
            onDone={(result) => {
              setMessage(result);
              if (result.ok) setDeleted(true);
            }}
          />
        </div>
      )}
      <p role="status" className={message?.ok === false ? kit.error : styles.note}>
        {message?.message}
      </p>
    </>
  );
}
