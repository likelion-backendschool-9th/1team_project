import { ReportModal } from "@/app/_components/ui/ReportModal";
import { formatCount, formatRelativeTime } from "@/app/_lib/format";
import type { MiniHomeComment } from "@/app/_lib/types/miniHome";
import type { UserSummary } from "@/app/_lib/types/user";
import { reportGuestbook } from "../_actions";
import { GuestbookForm } from "./GuestbookForm";
import styles from "./Board.module.css";

type Props = {
  userId: number;
  comments: MiniHomeComment[];
  total: number;
  now: number;
  me?: UserSummary;
};

export function Guestbook({ userId, comments, total, now, me }: Props) {
  return (
    <section className={styles.board} aria-labelledby="guestbook-title">
      <div className={styles.header}>
        <h2 id="guestbook-title" className={styles.title}>
          <span aria-hidden="true">💬</span> 일촌평 / 방명록
        </h2>
        <span className={styles.total}>Total {formatCount(total)}</span>
      </div>
      <GuestbookForm userId={userId} />
      {comments.length === 0 ? (
        <p className={styles.empty}>첫 일촌평을 남겨 보세요.</p>
      ) : (
        <ul className={styles.list}>
          {comments.map((comment) => (
            <li key={comment.id} className={styles.row}>
              <span className={styles.text}>
                <strong className={styles.author}>{comment.author.nickname}:</strong>
                {comment.content}
              </span>
              {comment.secret ? (
                <span className={styles.lock}>
                  <span aria-hidden="true">🔒</span>
                  <span className="sr-only">비밀글</span>
                </span>
              ) : (
                <time className={styles.time} dateTime={comment.createdAt}>
                  {formatRelativeTime(comment.createdAt, now)}
                </time>
              )}
              {/* 내 방명록은 신고하지 않는다 — 로그인 상태이고 작성자가 본인이 아닐 때만 표시 */}
              {me && me.id !== comment.author.id && (
                <ReportModal
                  label="신고"
                  title="방명록 신고"
                  onConfirm={reportGuestbook.bind(null, userId, comment.id)}
                />
              )}
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
