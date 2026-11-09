"use client";

import { useState, useTransition } from "react";
import { CommentList } from "@/app/(user)/_components/CommentList";
import kit from "@/app/_components/ui/kit.module.css";
import type { PostComment } from "@/app/_lib/types/post";
import type { UserSummary } from "@/app/_lib/types/user";

type Result = { ok: boolean; message: string };

type Props = {
  postId: number;
  initialCursor: string;
  now: number;
  me?: UserSummary;
  maxCommentLength: number;
  loadMore: (postId: number, cursor: string) => Promise<{
    ok: boolean;
    message: string;
    comments: PostComment[];
    hasNext: boolean;
    nextCursor: string | null;
  }>;
  updateComment: (commentId: number, content: string) => Promise<Result>;
  deleteComment: (commentId: number) => Promise<Result>;
  likeComment: (commentId: number, like: boolean) => Promise<Result>;
  replyComment: (parentId: number, content: string) => Promise<Result & { data?: PostComment | null }>;
};

export function PostCommentLoadMore({
  postId,
  initialCursor,
  now,
  me,
  maxCommentLength,
  loadMore,
  ...actions
}: Props) {
  const [extraComments, setExtraComments] = useState<PostComment[]>([]);
  const [cursor, setCursor] = useState<string | null>(initialCursor);
  const [hasMore, setHasMore] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [pending, startTransition] = useTransition();

  function handleLoadMore() {
    if (!cursor) return;
    setError(null);
    startTransition(async () => {
      const result = await loadMore(postId, cursor);
      if (result.ok) {
        setExtraComments((prev) => [...prev, ...result.comments]);
        setHasMore(result.hasNext);
        setCursor(result.nextCursor);
      } else {
        setError(result.message);
      }
    });
  }

  return (
    <>
      {extraComments.length > 0 && (
        <CommentList
          comments={extraComments}
          now={now}
          me={me}
          maxCommentLength={maxCommentLength}
          {...actions}
        />
      )}
      {hasMore && (
        <div>
          <button type="button" className={kit.btnGhost} disabled={pending} onClick={handleLoadMore}>
            댓글 더 보기
          </button>
          {error && <p className={kit.error}>{error}</p>}
        </div>
      )}
    </>
  );
}
