"use client";

import Link from "next/link";
import { useState, useTransition } from "react";
import { Avatar } from "@/app/_components/ui/Avatar";
import kit from "@/app/_components/ui/kit.module.css";
import { formatRelativeTime } from "@/app/_lib/format";
import type { AppNotification, NotificationType } from "@/app/_lib/types/notification";
import { markRead } from "../_actions";
import styles from "./Notifications.module.css";

const icons: Record<NotificationType, string> = {
  like: "❤️",
  comment: "💬",
  follow: "👋",
  guestbook: "📖",
  repost: "🔁",
  subscribe: "⭐",
};

type Props = {
  notifications: AppNotification[];
  now: number;
};

export function NotificationList({ notifications, now }: Props) {
  const [readIds, setReadIds] = useState<Set<number>>(new Set());
  const [message, setMessage] = useState<string | null>(null);
  const [pending, startTransition] = useTransition();
  const isRead = (item: AppNotification) => item.read || readIds.has(item.id);
  const unread = notifications.filter((item) => !isRead(item));

  function read(ids: number[]) {
    setReadIds((current) => new Set([...current, ...ids]));
    startTransition(async () => {
      const result = await markRead(ids);
      setMessage(result.ok ? null : result.message);
    });
  }

  if (notifications.length === 0) return <p className={kit.empty}>새 알림이 없어요.</p>;

  return (
    <section className={kit.card} aria-label="알림 목록">
      <div className={kit.cardHeader}>
        <p className={kit.faint} role="status">
          안 읽은 알림 {unread.length}개
        </p>
        <button
          type="button"
          className={`${kit.btnGhost} ${kit.sm}`}
          disabled={pending || unread.length === 0}
          onClick={() => read(unread.map((item) => item.id))}
        >
          모두 읽음 처리
        </button>
      </div>
      <ul className={kit.list}>
        {notifications.map((item) => (
          <li key={item.id}>
            <Link
              href={item.href}
              className={`${kit.listItem} ${isRead(item) ? "" : styles.unread}`}
              onClick={() => !isRead(item) && read([item.id])}
            >
              <span className={styles.avatar}>
                <Avatar name={item.actor.nickname} size={44} tone="light" />
                <span className={styles.icon} aria-hidden="true">
                  {icons[item.type]}
                </span>
              </span>
              <span className={kit.grow}>
                <span className={styles.text}>
                  <strong>{item.actor.nickname}</strong>
                  {item.message}
                </span>
                <time className={kit.faint} dateTime={item.createdAt}>
                  {formatRelativeTime(item.createdAt, now)}
                </time>
              </span>
              {!isRead(item) && <span className={styles.dot} role="img" aria-label="안 읽음" />}
            </Link>
          </li>
        ))}
      </ul>
      <p role="alert" className={kit.error}>
        {message}
      </p>
    </section>
  );
}
