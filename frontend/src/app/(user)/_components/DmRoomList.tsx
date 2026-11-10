"use client";

import { Avatar } from "@/app/_components/ui/Avatar";
import { formatRelativeTime } from "@/app/_lib/format";
import type { DmRoom } from "@/app/_lib/types/dm";
import type { UserSummary } from "@/app/_lib/types/user";
import styles from "./Dm.module.css";

type Props = {
  rooms: DmRoom[];
  now: number;
  activeId: number | null;
  onSelect: (user: UserSummary) => void;
};

export function DmRoomList({ rooms, now, activeId, onSelect }: Props) {
  return (
    <nav aria-label="대화 목록" className={styles.rooms}>
      {rooms.length === 0 && <p className={styles.loading}>아직 대화가 없어요.</p>}
      <ul>
        {rooms.map((room) => {
          const active = room.user.id === activeId;
          return (
            <li key={room.user.id}>
              <button
                type="button"
                className={`${styles.room} ${active ? styles.activeRoom : ""}`}
                aria-current={active ? "true" : undefined}
                onClick={() => onSelect(room.user)}
              >
                <span className={styles.avatarWrap}>
                  <Avatar name={room.user.nickname} size={44} />
                  {room.online && <span className={styles.online} role="img" aria-label="접속 중" />}
                </span>
                <span className={styles.roomBody}>
                  <span className={styles.roomTop}>
                    <strong>{room.user.nickname}</strong>
                    <time dateTime={room.lastAt}>{formatRelativeTime(room.lastAt, now)}</time>
                  </span>
                  <span className={styles.preview}>{room.lastMessage}</span>
                </span>
                {room.unreadCount > 0 && (
                  <span className={styles.unread}>
                    {room.unreadCount}
                    <span className="sr-only">개 안 읽음</span>
                  </span>
                )}
              </button>
            </li>
          );
        })}
      </ul>
    </nav>
  );
}
