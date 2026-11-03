import { notFound } from "next/navigation";
import { connection } from "next/server";
import { Avatar } from "@/app/_components/ui/Avatar";
import { ConfirmButton } from "@/app/_components/ui/ConfirmButton";
import kit from "@/app/_components/ui/kit.module.css";
import { formatRelativeTime } from "@/app/_lib/format";
import { getMe } from "@/app/(user)/_lib/api";
import { deleteGuestbook } from "../../_actions";
import { getGuestbook, getProfile, getServerTime } from "../../_lib/api";
import { parseUserId } from "../../_lib/parse";
import { GuestbookWriteForm } from "./GuestbookWriteForm";
import styles from "./Guestbook.module.css";

type Props = {
  params: Promise<{ userId: string }>;
};

export async function GuestbookContent({ params }: Props) {
  await connection();
  const userId = parseUserId((await params).userId);
  const profile = userId ? await getProfile(userId) : null;
  if (!userId || !profile) notFound();

  const [me, entries, now] = await Promise.all([getMe(), getGuestbook(userId), getServerTime()]);
  const isOwner = me?.id === userId;

  return (
    <section className={kit.card} aria-labelledby="guestbook-list-title">
      <div className={kit.cardHeader}>
        <h2 id="guestbook-list-title" className={kit.cardTitle}>
          <span aria-hidden="true">📖</span> {profile.nickname}님의 방명록
        </h2>
        <span className={kit.faint}>총 {entries.length}개</span>
      </div>
      {!isOwner && <GuestbookWriteForm userId={userId} />}
      {entries.length === 0 ? (
        <p className={kit.empty}>아직 방명록이 없어요. 첫 글을 남겨 보세요.</p>
      ) : (
        <ol className={styles.list}>
          {entries.map((entry) => {
            const canDelete = isOwner || entry.author.id === me?.id;
            const hidden = entry.secret && !canDelete;
            return (
              <li key={entry.id} className={styles.entry}>
                <Avatar name={entry.author.nickname} size={40} tone="light" />
                <div className={kit.grow}>
                  <p className={styles.meta}>
                    <strong>{entry.author.nickname}</strong>
                    <time dateTime={entry.createdAt}>{formatRelativeTime(entry.createdAt, now)}</time>
                    {entry.secret && <span className={kit.pill}>🔒 비밀글</span>}
                  </p>
                  <p className={hidden ? styles.hidden : styles.content}>
                    {hidden ? "홈피 주인과 작성자만 볼 수 있는 비밀글이에요." : entry.content}
                  </p>
                </div>
                {canDelete && (
                  <ConfirmButton
                    label="삭제"
                    title="방명록을 삭제할까요?"
                    description="삭제한 방명록은 되돌릴 수 없어요."
                    confirmLabel="삭제"
                    danger
                    className={`${kit.btnGhost} ${kit.sm}`}
                    onConfirm={deleteGuestbook.bind(null, entry.id)}
                  />
                )}
              </li>
            );
          })}
        </ol>
      )}
    </section>
  );
}
