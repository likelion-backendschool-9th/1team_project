"use client";

import Link from "next/link";
import { useId, useState, useTransition, type ReactNode } from "react";
import { Avatar } from "@/app/_components/ui/Avatar";
import { HeartIcon } from "@/app/_components/ui/Icons";
import kit from "@/app/_components/ui/kit.module.css";
import { formatRelativeTime } from "@/app/_lib/format";
import type { UserSummary } from "@/app/_lib/types/user";
import { CommentEditor } from "./CommentEditor";
import styles from "./CommentList.module.css";

type Comment = {
  id: number;
  author: UserSummary;
  content: string;
  createdAt: string;
  likeCount: number;
  liked?: boolean;
  replies?: Comment[];
  label?: ReactNode;
};

type Result = { ok: boolean; message: string };

type Actions = {
  updateComment?: (commentId: number, content: string) => Promise<Result>;
  deleteComment?: (commentId: number) => Promise<Result>;
  likeComment?: (commentId: number, like: boolean) => Promise<Result>;
  replyComment?: (parentId: number, content: string) => Promise<Result>;
};

type Props = Actions & {
  comments: Comment[];
  now: number;
  me?: UserSummary;
  maxCommentLength?: number;
};

export function CommentList({ comments, now, me, maxCommentLength = 300, ...actions }: Props) {
  if (comments.length === 0)
    return <p className={styles.empty}>아직 댓글이 없어요. 첫 댓글을 남겨 보세요.</p>;

  return (
    <ul className={styles.list}>
      {comments.map((comment) => (
        <CommentThread key={comment.id} comment={comment} now={now} me={me} actions={actions} maxCommentLength={maxCommentLength} />
      ))}
    </ul>
  );
}

type ThreadProps = {
  comment: Comment;
  now: number;
  me?: UserSummary;
  actions: Actions;
  maxCommentLength: number;
};

// 댓글 하나와 그 아래 답글
function CommentThread({ comment, now, me, actions, maxCommentLength }: ThreadProps) {
  const [replies, setReplies] = useState(comment.replies ?? []);
  const [replyTo, setReplyTo] = useState<string | null>(null);
  const canReply = Boolean(me && actions.replyComment);

  // 답글에 다는 답글은 원 댓글 아래에 붙이고, 누구에게 답하는지 @닉네임으로 남긴다.
  function openReply(target?: UserSummary) {
    setReplyTo(target ? `@${target.nickname} ` : "");
  }

  return (
    <li className={styles.item}>
      <CommentRow
        comment={comment}
        now={now}
        me={me}
        actions={actions}
        onReply={canReply ? () => openReply() : undefined}
      />
      {(replies.length > 0 || replyTo !== null) && (
        <ul className={styles.replies}>
          {replies.map((reply) => (
            <li key={reply.id} className={styles.reply}>
              <CommentRow
                comment={reply}
                now={now}
                me={me}
                actions={actions}
                onReply={canReply ? () => openReply(reply.author) : undefined}
              />
            </li>
          ))}
          {replyTo !== null && me && actions.replyComment && (
            <li className={styles.reply}>
              <ReplyForm
                key={replyTo}
                initial={replyTo}
                maxLength={maxCommentLength}
                onCancel={() => setReplyTo(null)}
                onSubmit={async (content) => {
                  const result = await actions.replyComment!(comment.id, content);
                  if (result.ok) {
                    // 목 모드에서는 서버에 남지 않으므로 바로 목록에 붙여 보여 준다.
                    setReplies((items) => [
                      ...items,
                      {
                        id: -Date.now(),
                        author: me,
                        content,
                        createdAt: new Date(now).toISOString(),
                        likeCount: 0,
                        liked: false,
                      },
                    ]);
                    setReplyTo(null);
                  }
                  return result;
                }}
              />
            </li>
          )}
        </ul>
      )}
    </li>
  );
}

type RowProps = {
  comment: Comment;
  now: number;
  me?: UserSummary;
  actions: Actions;
  onReply?: () => void;
};

function CommentRow({ comment, now, me, actions, onReply }: RowProps) {
  const mine = comment.author.id === me?.id;
  // 방금 단 답글은 아직 서버 id가 없어 수정·삭제·좋아요를 막는다.
  const saved = comment.id > 0;

  return (
    <div className={styles.row}>
      <Link href={`/mini-home/${comment.author.id}`} aria-label={`${comment.author.nickname} 미니홈피`}>
        <Avatar name={comment.author.nickname} size={36} tone="light" />
      </Link>
      <div className={styles.body}>
        <p className={styles.meta}>
          <Link href={`/mini-home/${comment.author.id}`} className={styles.name}>
            {comment.author.nickname}
          </Link>
          {comment.label}
          <time dateTime={comment.createdAt}>{formatRelativeTime(comment.createdAt, now)}</time>
        </p>
        {mine && saved && actions.updateComment && actions.deleteComment ? (
          <CommentEditor
            commentId={comment.id}
            content={comment.content}
            updateComment={actions.updateComment}
            deleteComment={actions.deleteComment}
            onReply={onReply}
          />
        ) : (
          <>
            <p className={styles.content}>{comment.content}</p>
            {onReply && (
              <div className={styles.mine}>
                <button type="button" className={styles.mineButton} onClick={onReply}>
                  답글
                </button>
              </div>
            )}
          </>
        )}
      </div>
      <LikeButton
        commentId={comment.id}
        liked={comment.liked ?? false}
        likeCount={comment.likeCount}
        likeComment={saved ? actions.likeComment : undefined}
      />
    </div>
  );
}

type LikeProps = {
  commentId: number;
  liked: boolean;
  likeCount: number;
  likeComment?: (commentId: number, like: boolean) => Promise<Result>;
};

function LikeButton({ commentId, liked, likeCount, likeComment }: LikeProps) {
  const [state, setState] = useState({ active: liked, count: likeCount });
  const [error, setError] = useState<string | null>(null);
  const [pending, startTransition] = useTransition();

  if (!likeComment)
    return (
      <span className={styles.likes}>
        <HeartIcon />
        <span className="sr-only">좋아요</span>
        {state.count}
      </span>
    );

  function toggle() {
    const previous = state;
    const next = !previous.active;
    setState({ active: next, count: previous.count + (next ? 1 : -1) });
    setError(null);
    startTransition(async () => {
      const result = await likeComment!(commentId, next);
      if (!result.ok) {
        setState(previous);
        setError(result.message);
      }
    });
  }

  return (
    <div className={styles.likeBox}>
      <button
        type="button"
        className={`${styles.likes} ${styles.likeButton} ${state.active ? styles.liked : ""}`}
        aria-pressed={state.active}
        disabled={pending}
        onClick={toggle}
      >
        <span key={String(state.active)} className={state.active ? styles.pop : undefined}>
          <HeartIcon filled={state.active} />
        </span>
        <span className="sr-only">좋아요</span>
        {state.count}
      </button>
      <p role="alert" className={kit.error}>
        {error}
      </p>
    </div>
  );
}

type ReplyFormProps = {
  initial: string;
  maxLength: number;
  onSubmit: (content: string) => Promise<Result>;
  onCancel: () => void;
};

function ReplyForm({ initial, maxLength, onSubmit, onCancel }: ReplyFormProps) {
  const id = useId();
  const [draft, setDraft] = useState(initial);
  const [error, setError] = useState<string | null>(null);
  const [pending, startTransition] = useTransition();

  function submit() {
    const text = draft.trim();
    if (!text) return setError("답글을 입력해 주세요.");
    if (text.length > maxLength) return setError(`${maxLength}자 이하로 입력해 주세요.`);
    setError(null);
    startTransition(async () => {
      const result = await onSubmit(text);
      if (!result.ok) setError(result.message);
    });
  }

  return (
    <div className={styles.editBox}>
      <label htmlFor={id} className="sr-only">
        답글 쓰기
      </label>
      <textarea
        id={id}
        value={draft}
        maxLength={maxLength}
        rows={2}
        autoFocus
        placeholder="답글을 남겨 주세요"
        className={kit.textarea}
        style={{ minHeight: 64 }}
        aria-invalid={error ? true : undefined}
        onChange={(event) => setDraft(event.target.value)}
      />
      <div className={kit.row}>
        <button type="button" className={`${kit.btnPrimary} ${kit.sm}`} disabled={pending} onClick={submit}>
          답글 등록
        </button>
        <button type="button" className={`${kit.btn} ${kit.sm}`} onClick={onCancel}>
          취소
        </button>
      </div>
      <p role="alert" className={kit.error}>
        {error}
      </p>
    </div>
  );
}
