import Link from "next/link";
import type { ReactNode } from "react";
import { Avatar } from "@/app/_components/ui/Avatar";
import { HeartIcon } from "@/app/_components/ui/Icons";
import { formatRelativeTime } from "@/app/_lib/format";
import type { UserSummary } from "@/app/_lib/types/user";
import styles from "./CommentList.module.css";

type Comment = {
  id: number;
  author: UserSummary;
  content: string;
  createdAt: string;
  likeCount: number;
  label?: ReactNode;
};

type Props = {
  comments: Comment[];
  now: number;
};

export function CommentList({ comments, now }: Props) {
  if (comments.length === 0)
    return <p className={styles.empty}>아직 댓글이 없어요. 첫 댓글을 남겨 보세요.</p>;

  return (
    <ul className={styles.list}>
      {comments.map((comment) => (
        <li key={comment.id} className={styles.item}>
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
            <p className={styles.content}>{comment.content}</p>
          </div>
          <span className={styles.likes}>
            <HeartIcon />
            <span className="sr-only">좋아요</span>
            {comment.likeCount}
          </span>
        </li>
      ))}
    </ul>
  );
}
