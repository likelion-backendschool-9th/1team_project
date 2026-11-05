import Link from "next/link";
import { notFound } from "next/navigation";
import { connection } from "next/server";
import { CommentForm } from "@/app/_components/ui/CommentForm";
import { LoginModal } from "@/app/_components/ui/LoginModal";
import kit from "@/app/_components/ui/kit.module.css";
import { CommentList } from "@/app/(user)/_components/CommentList";
import { getMe } from "@/app/(user)/_lib/api";
import { FeedCard } from "../../../_components/FeedCard";
import {
  createComment,
  deleteFeedComment,
  replyFeedComment,
  toggleFeedCommentLike,
  updateFeed,
  updateFeedComment,
} from "../_actions";
import { FeedEditForm } from "./FeedEditForm";
import { getFeed, getFeedComments, getServerTime } from "../_lib/api";

type Props = {
  params: Promise<{ feedId: string }>;
  searchParams: Promise<Record<string, string | string[] | undefined>>;
};

export async function FeedDetail({ params, searchParams }: Props) {
  await connection();
  const feedId = Number((await params).feedId);
  const feed = Number.isSafeInteger(feedId) && feedId > 0 ? await getFeed(feedId) : null;
  if (!feed) notFound();

  const [me, comments, now, query] = await Promise.all([
    getMe(),
    getFeedComments(feedId),
    getServerTime(),
    searchParams,
  ]);
  const editing = query.edit === "1" && feed.author.id === me?.id;

  return (
    <div className={kit.stack}>
      <Link href="/main" className={kit.more}>
        ← 피드로 돌아가기
      </Link>
      {editing ? (
        <FeedEditForm action={updateFeed.bind(null, feed.id)} feedId={feed.id} content={feed.content} />
      ) : (
        <FeedCard feed={feed} myId={me?.id} now={now} />
      )}
      <section id="comments" className={`${kit.card} ${kit.stack}`} aria-labelledby="comments-title">
        <h2 id="comments-title" className={kit.cardTitle}>
          댓글 {comments.length}
        </h2>
        {me ? (
          <CommentForm action={createComment.bind(null, feedId)} />
        ) : (
          <LoginModal />
        )}
        <CommentList
          comments={comments}
          now={now}
          me={me ?? undefined}
          updateComment={updateFeedComment}
          deleteComment={deleteFeedComment}
          likeComment={toggleFeedCommentLike}
          replyComment={replyFeedComment.bind(null, feedId)}
        />
      </section>
    </div>
  );
}
