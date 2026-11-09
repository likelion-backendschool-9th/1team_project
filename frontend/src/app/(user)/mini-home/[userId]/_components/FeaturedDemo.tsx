import Link from "next/link";
import { ArrowRightIcon } from "@/app/_components/ui/Icons";
import { CommentForm } from "@/app/_components/ui/CommentForm";
import type { MiniHomeComment, MiniHomeDemo } from "@/app/_lib/types/miniHome";
import { CommentList } from "@/app/(user)/_components/CommentList";
import type { UserSummary } from "@/app/_lib/types/user";
import {
  createDemoComment,
  deleteDemoComment,
  replyDemoComment,
  toggleDemoCommentLike,
  updateDemoComment,
} from "../_actions";
import { DemoArt } from "./DemoArt";
import { FeaturedActions } from "./FeaturedActions";
import { FeaturedBody } from "./FeaturedBody";
import { LaterButton } from "./LaterButton";
import styles from "./FeaturedDemo.module.css";

type Props = {
  demo: MiniHomeDemo | undefined;
  variant: number;
  postId: number | null;
  comments: MiniHomeComment[];
  me: UserSummary;
  now: number;
  /** 내 미니홈피면 데모가 없을 때 데모를 넣으라고 안내한다 */
  isOwner?: boolean;
};

export function FeaturedDemo({ demo, variant, postId, comments, me, now, isOwner = false }: Props) {
  return (
    <section className={styles.card} aria-labelledby="featured-title">
      <div className={styles.header}>
        <h2 id="featured-title" className={styles.title}>
          <span className={styles.live} aria-hidden="true" />
          대표 데모 (LIVE SANDBOX)
        </h2>
        <span className={styles.hint}>브라우저 내 직접 인터랙션 지원</span>
      </div>

      {demo ? (
        <FeaturedBody
          // 실행은 실제 데모 화면(샌드박스 iframe)에서 한다.
          runHref={`/mini-home/${demo.author.id}/demos/${demo.id}`}
          stage={
            <>
              {demo.thumbnail ? (
                // 썸네일이 있는 데모는 그 그림을 크게 보여 준다. 장식이라 alt는 비운다.
                // eslint-disable-next-line @next/next/no-img-element
                <img src={demo.thumbnail} alt="" className={`${styles.canvas} ${styles.mascot}`} />
              ) : (
                <DemoArt variant={variant} caption={demo.engine} className={styles.canvas} />
              )}
              <span className={styles.badge}>
                <span className={styles.badgeDot} aria-hidden="true" />
                {demo.title}
              </span>
            </>
          }
          summary={
            <div>
              <h3 className={styles.name}>{demo.title}</h3>
              <p className={styles.engine}>{demo.engine}</p>
            </div>
          }
          actions={
            <FeaturedActions demoId={demo.id} likeCount={demo.likeCount} commentCount={comments.length}>
              <CommentForm
                action={createDemoComment.bind(null, demo.id)}
                placeholder="데모를 찍먹해 본 소감을 남겨 주세요"
              />
              <CommentList
                comments={comments.map((comment) => ({ ...comment, likeCount: 0 }))}
                now={now}
                me={me}
                updateComment={updateDemoComment}
                deleteComment={deleteDemoComment}
                likeComment={toggleDemoCommentLike}
                replyComment={replyDemoComment.bind(null, demo.id)}
              />
            </FeaturedActions>
          }
          description={<p className={styles.desc}>{demo.description}</p>}
          extra={
            <>
              <div className={styles.links}>
                <LaterButton title={demo.title} />
                {postId ? (
                  <Link href={`/blog/posts/${postId}`} className={styles.making}>
                    제작기 글
                    <ArrowRightIcon />
                  </Link>
                ) : (
                  <span className={`${styles.making} ${styles.disabled}`} aria-disabled="true">
                    제작기 글 없음
                  </span>
                )}
              </div>
              <p className={styles.rule}>업로드 규격: zip · html/css/js 단일 구동 지원</p>
            </>
          }
        />
      ) : isOwner ? (
        <p className={styles.empty}>
          아직 공개된 데모가 없어요. 데모를 넣어주세요.
          <br />
          <Link href="/demos/upload">데모 올리기</Link> · <Link href={`/mini-home/${me.id}/edit`}>공개할 데모 고르기</Link>
        </p>
      ) : (
        <p className={styles.empty}>아직 공개된 데모가 없어요.</p>
      )}
    </section>
  );
}
