import Link from "next/link";
import { notFound } from "next/navigation";
import { connection } from "next/server";
import { CommentForm } from "@/app/_components/ui/CommentForm";
import kit from "@/app/_components/ui/kit.module.css";
import { CommentList } from "@/app/(user)/_components/CommentList";
import { getMe } from "@/app/(user)/_lib/api";
import { FeedCard } from "../../../_components/FeedCard";
import { createComment } from "../_actions";
import { getFeed, getFeedComments, getServerTime } from "../_lib/api";

type Props = {
  params: Promise<{ feedId: string }>;
};

export async function FeedDetail({ params }: Props) {
  await connection();
  const feedId = Number((await params).feedId);
  const feed = Number.isSafeInteger(feedId) && feedId > 0 ? await getFeed(feedId) : null;
  if (!feed) notFound();

  const [me, comments, now] = await Promise.all([getMe(), getFeedComments(feedId), getServerTime()]);

  return (
    <div className={kit.stack}>
      <Link href="/main" className={kit.more}>
        ← 피드로 돌아가기
      </Link>
      <FeedCard feed={feed} myId={me.id} now={now} />
      <section id="comments" className={`${kit.card} ${kit.stack}`} aria-labelledby="comments-title">
        <h2 id="comments-title" className={kit.cardTitle}>
          댓글 {comments.length}
        </h2>
        <CommentForm action={createComment.bind(null, feedId)} />
        <CommentList comments={comments} now={now} />
      </section>
    </div>
  );
}
