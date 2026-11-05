"use client";

import Link from "next/link";
import { useEffect, useId, useRef, useState, useTransition, type ReactNode } from "react";
import { Avatar } from "@/app/_components/ui/Avatar";
import { ChipIcon } from "@/app/_components/ui/ChipIcon";
import { BellIcon } from "@/app/_components/ui/Icons";
import { formatRelativeTime } from "@/app/_lib/format";
import type { AppNotification, NotificationType } from "@/app/_lib/types/notification";
import { markNotificationsRead } from "../_actions";
import styles from "./NotificationBell.module.css";

const icons: Record<NotificationType, ReactNode> = {
  like: "❤️",
  comment: "💬",
  follow: "👋",
  guestbook: "📖",
  repost: "🔁",
  subscribe: "⭐",
  sponsor: <ChipIcon size={16} />,
};

type Props = {
  notifications: AppNotification[];
  unreadCount: number;
  now: number;
  className: string;
};

// 알림은 별도 페이지 없이 버튼 아래 패널로 연다.
export function NotificationBell({ notifications, unreadCount, now, className }: Props) {
  const [open, setOpen] = useState(false);
  const [readIds, setReadIds] = useState<Set<number>>(new Set());
  const [, startTransition] = useTransition();
  const panelId = useId();
  const wrapRef = useRef<HTMLDivElement>(null);
  const buttonRef = useRef<HTMLButtonElement>(null);
  const isRead = (item: AppNotification) => item.read || readIds.has(item.id);
  const unread = notifications.filter((item) => !isRead(item));
  // 배지 숫자는 안 읽은 알림 수 API 값에서 이번에 읽은 만큼만 뺀다.
  const badge = Math.max(0, unreadCount - readIds.size);

  useEffect(() => {
    if (!open) return;
    function handlePointer(event: PointerEvent) {
      if (!wrapRef.current?.contains(event.target as Node)) setOpen(false);
    }
    function handleKey(event: KeyboardEvent) {
      if (event.key === "Escape") {
        setOpen(false);
        buttonRef.current?.focus();
      }
    }
    document.addEventListener("pointerdown", handlePointer);
    document.addEventListener("keydown", handleKey);
    return () => {
      document.removeEventListener("pointerdown", handlePointer);
      document.removeEventListener("keydown", handleKey);
    };
  }, [open]);

  function read(ids: number[]) {
    if (ids.length === 0) return;
    setReadIds((current) => new Set([...current, ...ids]));
    startTransition(async () => {
      await markNotificationsRead(ids);
    });
  }

  return (
    <div ref={wrapRef} className={styles.wrap}>
      <button
        ref={buttonRef}
        type="button"
        className={className}
        aria-expanded={open}
        aria-controls={panelId}
        onClick={() => setOpen((value) => !value)}
      >
        <BellIcon />
        알림
        {badge > 0 && (
          <span className={styles.badge}>
            {badge}
            <span className="sr-only">건 읽지 않음</span>
          </span>
        )}
      </button>
      <div id={panelId} className={styles.panel} hidden={!open} role="region" aria-label="알림 목록">
        <div className={styles.header}>
          <p className={styles.title}>알림</p>
          <button
            type="button"
            className={styles.readAll}
            disabled={unread.length === 0}
            onClick={() => read(unread.map((item) => item.id))}
          >
            모두 읽음
          </button>
        </div>
        {notifications.length === 0 ? (
          <p className={styles.empty}>새 알림이 없어요.</p>
        ) : (
          <ul className={styles.list}>
            {notifications.map((item) => (
              <li key={item.id}>
                <Link
                  href={item.href}
                  className={`${styles.item} ${isRead(item) ? "" : styles.unread}`}
                  onClick={() => {
                    if (!isRead(item)) read([item.id]);
                    setOpen(false);
                  }}
                >
                  <span className={styles.avatar}>
                    <Avatar name={item.actor.nickname} size={36} tone="light" />
                    <span className={styles.icon} aria-hidden="true">
                      {icons[item.type]}
                    </span>
                  </span>
                  <span className={styles.body}>
                    <span className={styles.text}>
                      <strong>{item.actor.nickname}</strong>
                      {item.message}
                    </span>
                    <time dateTime={item.createdAt}>{formatRelativeTime(item.createdAt, now)}</time>
                  </span>
                  {!isRead(item) && <span className={styles.dot} role="img" aria-label="안 읽음" />}
                </Link>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}
