"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { Suspense } from "react";
import { Avatar } from "@/app/_components/ui/Avatar";
import { formatRelativeTime } from "@/app/_lib/format";
import type { DmRoom } from "@/app/_lib/types/dm";
import styles from "./Dm.module.css";

type Props = {
  rooms: DmRoom[];
  now: number;
};

function Rooms({ rooms, now, pathname }: Props & { pathname: string | null }) {
  return (
    <nav aria-label="대화 목록" className={styles.rooms}>
      <p className={styles.roomsTitle}>메시지</p>
      <ul>
        {rooms.map((room) => {
          const href = `/dm/${room.user.id}`;
          const active = pathname === href;
          return (
            <li key={room.user.id}>
              <Link
                href={href}
                className={`${styles.room} ${active ? styles.activeRoom : ""}`}
                aria-current={active ? "page" : undefined}
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
              </Link>
            </li>
          );
        })}
      </ul>
    </nav>
  );
}

function ActiveRooms(props: Props) {
  return <Rooms {...props} pathname={usePathname()} />;
}

export function RoomLinks(props: Props) {
  return (
    <Suspense fallback={<Rooms {...props} pathname={null} />}>
      <ActiveRooms {...props} />
    </Suspense>
  );
}
