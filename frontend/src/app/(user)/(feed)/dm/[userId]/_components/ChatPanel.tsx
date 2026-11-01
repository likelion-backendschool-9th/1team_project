"use client";

import { useEffect, useRef, useState, type FormEvent } from "react";
import type { DmMessage } from "@/app/_lib/types/dm";
import styles from "../../_components/Dm.module.css";

type Props = {
  myId: number;
  partnerName: string;
  initialMessages: DmMessage[];
};

const MAX_LENGTH = 1000;

function timeLabel(iso: string) {
  const date = new Date(iso);
  return `${String(date.getHours()).padStart(2, "0")}:${String(date.getMinutes()).padStart(2, "0")}`;
}

// 실제 전송은 WebSocket(STOMP /app/dm.send)으로 붙인다. 지금은 화면에만 추가한다.
export function ChatPanel({ myId, partnerName, initialMessages }: Props) {
  const [messages, setMessages] = useState(initialMessages);
  const [draft, setDraft] = useState("");
  const listRef = useRef<HTMLOListElement>(null);

  useEffect(() => {
    const list = listRef.current;
    if (list) list.scrollTop = list.scrollHeight;
  }, [messages.length]);

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const content = draft.trim();
    if (!content) return;
    setMessages((list) => [
      ...list,
      {
        id: Date.now(),
        senderId: myId,
        content,
        createdAt: new Date().toISOString(),
        read: false,
      },
    ]);
    setDraft("");
  }

  return (
    <>
      <p className={styles.connection} role="status">
        <span className={styles.connectionDot} aria-hidden="true" />
        실시간 연결됨 (목업)
      </p>
      <ol ref={listRef} className={styles.messages} aria-label={`${partnerName}님과의 메시지`} tabIndex={0}>
        {messages.map((message) => {
          const mine = message.senderId === myId;
          return (
            <li key={message.id} className={`${styles.message} ${mine ? styles.mine : ""}`}>
              <span className="sr-only">{mine ? "나: " : `${partnerName}: `}</span>
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
          onChange={(event) => setDraft(event.target.value)}
          onKeyDown={(event) => {
            if (event.key === "Enter" && !event.shiftKey && !event.nativeEvent.isComposing) {
              event.preventDefault();
              event.currentTarget.form?.requestSubmit();
            }
          }}
        />
        <button type="submit" className={styles.send} disabled={!draft.trim()}>
          전송
        </button>
      </form>
    </>
  );
}
