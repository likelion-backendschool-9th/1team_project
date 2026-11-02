import Link from "next/link";
import { notFound, redirect } from "next/navigation";
import { connection } from "next/server";
import { Avatar } from "@/app/_components/ui/Avatar";
import { CommentForm } from "@/app/_components/ui/CommentForm";
import { ConfirmButton } from "@/app/_components/ui/ConfirmButton";
import kit from "@/app/_components/ui/kit.module.css";
import { formatCount, formatDate } from "@/app/_lib/format";
import type { DemoFeedbackType } from "@/app/_lib/types/demo";
import { CommentList } from "@/app/(user)/_components/CommentList";
import { getMe } from "@/app/(user)/_lib/api";
import {
  createDemoComment,
  deleteDemo,
  deleteDemoComment,
  replyDemoComment,
  reportDemo,
  toggleDemoCommentLike,
  updateDemoComment,
} from "../_actions";
import { getDemo, getDemoComments, getServerTime } from "@/app/(user)/(feed)/demos/_lib/api";
import { DemoStage } from "./DemoStage";
import styles from "./DemoDetail.module.css";

type Props = {
  params: Promise<{ userId: string; demoId: string }>;
};

const feedbackLabels: Record<DemoFeedbackType, string> = {
  question: "❓ 질문",
  bug: "🐞 버그",
  praise: "👏 칭찬",
};

export async function DemoDetailView({ params }: Props) {
  await connection();
  const { userId, demoId: rawDemoId } = await params;
  const demoId = Number(rawDemoId);
  const demo = Number.isSafeInteger(demoId) && demoId > 0 ? await getDemo(demoId) : null;
  if (!demo) notFound();
  // 데모는 올린 사람의 미니홈피에서만 보여 준다. 다른 홈피 주소로 들어오면 작성자 홈피로 보낸다.
  if (String(demo.author.id) !== userId) redirect(`/mini-home/${demo.author.id}/demos/${demo.id}`);

  const [me, comments, now] = await Promise.all([getMe(), getDemoComments(demoId), getServerTime()]);
  const mine = me.id === demo.author.id;

  return (
    <div className={kit.stack}>
      <Link href={`/mini-home/${demo.author.id}/demos`} className={kit.more}>
        ← {demo.author.nickname}님의 데모
      </Link>

      <section className={`${kit.card} ${styles.hero}`} aria-labelledby="demo-title">
        <DemoStage
          demoId={demo.id}
          title={demo.title}
          seed={demo.id - 1}
          label={demo.runtime === "unity" ? "unity webgl" : demo.techTags[0]}
          initialPlays={demo.playCount}
          initialLikes={demo.likeCount}
          liked={demo.liked}
          bookmarked={demo.bookmarked}
        />
        <div className={styles.info}>
          <h2 id="demo-title" className={styles.title}>
            {demo.title}
          </h2>
          <p className={kit.muted}>{demo.description}</p>
          <ul className={kit.chips} aria-label="기술 태그">
            {demo.techTags.map((tag) => (
              <li key={tag} className={kit.pill}>
                #{tag}
              </li>
            ))}
          </ul>
          <dl className={styles.meta}>
            <div>
              <dt>실행 방식</dt>
              <dd>{demo.runtime === "unity" ? "Unity WebGL" : "html / css / js"}</dd>
            </div>
            <div>
              <dt>용량</dt>
              <dd>{demo.sizeMb.toFixed(1)}MB</dd>
            </div>
            <div>
              <dt>올린 날</dt>
              <dd>{formatDate(demo.createdAt)}</dd>
            </div>
            <div>
              <dt>댓글</dt>
              <dd>{formatCount(comments.length)}</dd>
            </div>
          </dl>
          <div className={styles.author}>
            <Link href={`/mini-home/${demo.author.id}`} className={styles.authorLink}>
              <Avatar name={demo.author.nickname} size={40} />
              <span>
                <strong>{demo.author.nickname}</strong>
                <span className={kit.faint}> · {demo.author.techStacks[0]}</span>
              </span>
            </Link>
            {!mine && (
              <Link href={`/dm/${demo.author.id}`} className={`${kit.btnGhost} ${kit.sm}`}>
                DM 보내기
              </Link>
            )}
          </div>
          {mine ? (
            <ConfirmButton
              label="데모 삭제"
              title="이 데모를 삭제할까요?"
              description="댓글과 좋아요도 함께 사라지고, 데모 인벤토리 1칸이 비어요."
              confirmLabel="삭제"
              danger
              className={`${kit.btnDanger} ${kit.sm}`}
              onConfirm={deleteDemo.bind(null, demo.id)}
            />
          ) : (
            <ConfirmButton
              label="신고하기"
              title="이 데모를 신고할까요?"
              description="운영진이 확인 후 조치해요. 허위 신고는 제재될 수 있어요."
              confirmLabel="신고"
              danger
              className={styles.report}
              onConfirm={reportDemo.bind(null, demo.id)}
            />
          )}
        </div>
      </section>

      <section id="comments" className={`${kit.card} ${kit.stack}`} aria-labelledby="demo-comments-title">
        <h2 id="demo-comments-title" className={kit.cardTitle}>
          피드백 {comments.length}
        </h2>
        <CommentForm
          action={createDemoComment.bind(null, demo.id)}
          label="피드백 쓰기"
          placeholder="찍먹해 본 소감, 질문, 버그를 남겨 주세요"
        >
          <fieldset className={styles.types}>
            <legend className="sr-only">피드백 종류</legend>
            {(Object.keys(feedbackLabels) as DemoFeedbackType[]).map((type) => (
              <label key={type} className={styles.type}>
                <input type="radio" name="type" value={type} defaultChecked={type === "praise"} />
                {feedbackLabels[type]}
              </label>
            ))}
          </fieldset>
        </CommentForm>
        <CommentList
          comments={comments.map((comment) => ({
            ...comment,
            label: <span className={kit.pill}>{feedbackLabels[comment.type]}</span>,
          }))}
          now={now}
          me={me}
          updateComment={updateDemoComment}
          deleteComment={deleteDemoComment}
          likeComment={toggleDemoCommentLike}
          replyComment={replyDemoComment.bind(null, demo.id)}
        />
      </section>
    </div>
  );
}
