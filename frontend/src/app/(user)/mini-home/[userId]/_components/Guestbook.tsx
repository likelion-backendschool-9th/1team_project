import { formatCount, formatRelativeTime } from "@/app/_lib/format";
import type { MiniHomeComment } from "@/app/_lib/types/miniHome";
import { GuestbookForm } from "./GuestbookForm";
import styles from "./Board.module.css";

type Props = {
  userId: number;
  comments: MiniHomeComment[];
  total: number;
  now: number;
};

export function Guestbook({ userId, comments, total, now }: Props) {
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
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
