"use client";

import Link from "next/link";
import { useEffect, useRef, useState, type FormEvent } from "react";
import { Avatar } from "@/app/_components/ui/Avatar";
import type { DmMessage } from "@/app/_lib/types/dm";
import type { UserSummary } from "@/app/_lib/types/user";
import type { DmSocketStatus } from "../_hooks/useDmSocket";
import styles from "./Dm.module.css";

type Props = {
  myId: number;
  partner: UserSummary;
  online: boolean;
  /** 불러오는 중이면 null */
  messages: DmMessage[] | null;
  status: DmSocketStatus;
  error: string | null;
  /** 보내기에 성공하면 true. 연결이 끊겨 못 보냈으면 false */
  onSend: (content: string) => boolean;
};

const MAX_LENGTH = 1000;

const statusLabels: Record<DmSocketStatus, string> = {
  mock: "실시간 연결 꺼짐 (NEXT_PUBLIC_WS_URL 미설정 · 보낸 메시지는 이 화면에만 보여요)",
  connecting: "연결 중…",
  connected: "실시간 연결됨",
  disconnected: "연결이 끊겼어요. 다시 연결하는 중…",
};

function timeLabel(iso: string) {
  const date = new Date(iso);
  return `${String(date.getHours()).padStart(2, "0")}:${String(date.getMinutes()).padStart(2, "0")}`;
}

// 메시지 전송·수신은 WebSocket(STOMP /app/dm.send, /user/queue/dm)으로 한다.
// 서버로 보낸 메시지는 /user/queue/dm 으로 되돌아올 때 목록에 붙는다.
export function DmChat({ myId, partner, online, messages, status, error, onSend }: Props) {
  const [draft, setDraft] = useState("");
  const listRef = useRef<HTMLOListElement>(null);

  useEffect(() => {
    const list = listRef.current;
    if (list) list.scrollTop = list.scrollHeight;
  }, [messages?.length]);

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const content = draft.trim();
    // 연결이 끊긴 동안에는 입력을 지우지 않는다.
    if (!content || !onSend(content)) return;
    setDraft("");
  }

  return (
    <>
      <header className={styles.chatHeader}>
        <Avatar name={partner.nickname} size={40} />
        <div>
          <h2 className={styles.chatName}>{partner.nickname}</h2>
          <p className={styles.chatSub}>{online ? "접속 중" : "오프라인"}</p>
        </div>
        <Link href={`/mini-home/${partner.id}`} className={styles.homeLink}>
          미니홈피
        </Link>
      </header>
      <p className={styles.connection} role="status">
        <span className={styles.connectionDot} aria-hidden="true" />
        {statusLabels[status]}
      </p>
      <ol
        ref={listRef}
        className={styles.messages}
        aria-label={`${partner.nickname}님과의 메시지`}
        aria-busy={messages === null}
        tabIndex={0}
      >
        {messages === null && <li className={styles.loading}>대화를 불러오는 중…</li>}
        {messages?.map((message) => {
          const mine = message.senderId === myId;
          return (
            <li key={message.id} className={`${styles.message} ${mine ? styles.mine : ""}`}>
              <span className="sr-only">{mine ? "나: " : `${partner.nickname}: `}</span>
              <p className={styles.bubble}>{message.content}</p>
              <span className={styles.messageMeta}>
                {mine && !message.read && <span className={styles.unreadMark}>1</span>}
                <time dateTime={message.createdAt}>{timeLabel(message.createdAt)}</time>
              </span>
            </li>
          );
        })}
      </ol>
      <form className={styles.composer} onSubmit={handleSubmit}>
        <label htmlFor="dm-input" className="sr-only">
          메시지 입력
        </label>
        <textarea
          id="dm-input"
          rows={1}
          maxLength={MAX_LENGTH}
          value={draft}
          placeholder="메시지 입력 · Enter 전송"
          className={styles.input}
          aria-describedby="dm-error"
          onChange={(event) => setDraft(event.target.value)}
          onKeyDown={(event) => {
            if (event.key === "Enter" && !event.shiftKey && !event.nativeEvent.isComposing) {
              event.preventDefault();
              event.currentTarget.form?.requestSubmit();
            }
          }}
        />
        <button
          type="submit"
          className={styles.send}
          disabled={!draft.trim() || messages === null || status === "connecting" || status === "disconnected"}
        >
          전송
        </button>
      </form>
      <p id="dm-error" role="alert" className={styles.socketError}>
        {error}
      </p>
    </>
  );
}
