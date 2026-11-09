import Link from "next/link";
import { notFound } from "next/navigation";
import { connection } from "next/server";
import { Avatar } from "@/app/_components/ui/Avatar";
import { ArrowRightIcon } from "@/app/_components/ui/Icons";
import { CommentForm } from "@/app/_components/ui/CommentForm";
import { ConfirmButton } from "@/app/_components/ui/ConfirmButton";
import { ReportModal } from "@/app/_components/ui/ReportModal";
import kit from "@/app/_components/ui/kit.module.css";
import { formatCount, formatDate, formatWon } from "@/app/_lib/format";
import { chipsToWon } from "@/app/_lib/policy";
import { CommentList } from "@/app/(user)/_components/CommentList";
import { getMe } from "@/app/(user)/_lib/api";
import {
  createPostComment,
  deletePost,
  deletePostComment,
  loadMorePostComments,
  replyPostComment,
  reportPost,
  togglePostCommentLike,
  unlockPost,
  updatePostComment,
} from "../../../_actions";
import { getPost, getPostComments, getServerTime } from "../../../_lib/api";
import { categories } from "../../../_lib/query";
import { Markdown } from "../../../_components/Markdown";
import { PostActions } from "../../../_components/PostActions";
import { PostCommentLoadMore } from "./PostCommentLoadMore";
import styles from "./Post.module.css";

type Props = {
  params: Promise<{ postId: string }>;
};

export async function PostView({ params }: Props) {
  await connection();
  const postId = Number((await params).postId);
  const post = Number.isSafeInteger(postId) && postId > 0 ? await getPost(postId) : null;
  if (!post) notFound();

  const [me, { comments, hasNext, nextCursor }, now] = await Promise.all([getMe(), getPostComments(postId), getServerTime()]);
  const mine = me?.id === post.author.id;
  const locked = post.price > 0 && !post.purchased && !mine;
  const category = categories.find((item) => item.value === post.category)?.label;

  return (
    <div className={kit.stack}>
      <Link href="/blog" className={kit.more}>
        ← 블로그 홈
      </Link>
      <article className={`${kit.card} ${styles.article}`} aria-labelledby="post-title">
        <header className={styles.header}>
          <div className={kit.row}>
            {category && <span className={kit.pill}>{category}</span>}
            {post.seriesTitle && <span className={kit.pillAccent}>연재 · {post.seriesTitle}</span>}
            {post.price > 0 ? (
              <span className={kit.pillWarn}>🔒 {post.price}칩</span>
            ) : (
              <span className={kit.pillSuccess}>무료 공개</span>
            )}
          </div>
          <h2 id="post-title" className={styles.title}>
            {post.title}
          </h2>
          <div className={styles.byline}>
            <Link href={`/blog/${post.author.id}`} className={styles.author}>
              <Avatar name={post.author.nickname} size={40} />
              <span>
                <strong>{post.author.nickname}</strong>
                <span className={kit.faint} style={{ display: "block" }}>
                  <time dateTime={post.createdAt}>{formatDate(post.createdAt)}</time> · 읽는 시간 {post.readMinutes}분 · 조회{" "}
                  {formatCount(post.viewCount)}
                </span>
              </span>
            </Link>
            {mine ? (
              <div className={kit.row}>
                <Link href={`/blog/posts/${post.id}/edit`} className={`${kit.btnGhost} ${kit.sm}`}>
                  수정
                </Link>
                <ConfirmButton
                  label="삭제"
                  title="글을 삭제할까요?"
                  description="삭제한 글과 댓글은 되돌릴 수 없어요."
                  confirmLabel="삭제"
                  danger
                  className={`${kit.btnGhost} ${kit.sm}`}
                  onConfirm={deletePost.bind(null, post.id)}
                />
              </div>
            ) : (
              <ReportModal
                label="신고"
                title="이 글을 신고할까요?"
                className={styles.report}
                onConfirm={(reason) => reportPost(post.id, reason)}
              />
            )}
          </div>
        </header>

        <div className={locked ? styles.lockedBody : undefined}>
          <Markdown source={post.content} limit={locked ? 3 : undefined} />
        </div>

        {locked && (
          <section className={styles.paywall} aria-labelledby="paywall-title">
            <span className={styles.lock} aria-hidden="true">
              🔒
            </span>
            <h3 id="paywall-title" className={styles.paywallTitle}>
              이어지는 내용은 {post.price}칩({formatWon(chipsToWon(post.price))})으로 열람할 수 있어요
            </h3>
            <p className={kit.muted}>
              보유 칩 {formatCount(me?.chipBalance ?? 0)}개 · 열람 후 24시간 안에 코드가 동작하지 않으면 100% 환불돼요.
            </p>
            <div className={kit.row} style={{ justifyContent: "center" }}>
              <ConfirmButton
                label={`${post.price}칩으로 열람하기`}
                title={`${post.price}칩을 사용할까요?`}
                description={`사용 후 남는 칩: ${formatCount((me?.chipBalance ?? 0) - post.price)}개`}
                confirmLabel="열람하기"
                className={kit.btnPrimary}
                onConfirm={unlockPost.bind(null, post.id)}
              />
              <Link href={`/blog/${post.author.id}`} className={kit.btnGhost}>
                블로그 구독하기
              </Link>
            </div>
          </section>
        )}

        {post.demo && (
          <div className={styles.source}>
            <span aria-hidden="true">&lt;/&gt;</span>
            <div className={kit.grow}>
              <p className={kit.strong}>{post.demo.title}</p>
              <p className={kit.faint}>{post.demo.description}</p>
            </div>
            <Link href={`/demos/${post.demo.id}`} className={kit.btnPrimary}>
              소스 찍먹하기 <ArrowRightIcon />
            </Link>
          </div>
        )}

        <ul className={kit.chips} aria-label="태그">
          {post.tags.map((tag) => (
            <li key={tag} className={kit.pill}>
              #{tag}
            </li>
          ))}
        </ul>

        <PostActions
          postId={post.id}
          likeCount={post.likeCount}
          commentCount={post.commentCount}
          repostCount={post.repostCount}
          liked={post.liked}
          bookmarked={post.bookmarked}
          readMinutes={post.readMinutes}
          loggedIn={me !== null}
        />
      </article>

      <section id="comments" className={`${kit.card} ${kit.stack}`} aria-labelledby="post-comments-title">
        <h2 id="post-comments-title" className={kit.cardTitle}>
          댓글 {post.commentCount}
        </h2>
        <CommentForm action={createPostComment.bind(null, post.id)} maxLength={1000} />
        <CommentList
          comments={comments}
          now={now}
          me={me ?? undefined}
          maxCommentLength={1000}
          updateComment={updatePostComment}
          deleteComment={deletePostComment.bind(null, post.id)}
          likeComment={togglePostCommentLike}
          replyComment={replyPostComment.bind(null, post.id)}
        />
        {hasNext && nextCursor && (
          <PostCommentLoadMore
            postId={post.id}
            initialCursor={nextCursor}
            now={now}
            me={me ?? undefined}
            maxCommentLength={1000}
            loadMore={loadMorePostComments}
            updateComment={updatePostComment}
            deleteComment={deletePostComment.bind(null, post.id)}
            likeComment={togglePostCommentLike}
            replyComment={replyPostComment.bind(null, post.id)}
          />
        )}
      </section>
    </div>
  );
}
