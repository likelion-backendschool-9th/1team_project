"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import type { DmMessage, DmRoom } from "@/app/_lib/types/dm";
import type { UserSummary } from "@/app/_lib/types/user";
import { loadDmMessages, loadDmRooms } from "../_actions";
import { DmChat } from "./DmChat";
import { DmRoomList } from "./DmRoomList";
import { useRealtime, useRealtimeEvent, type DmSocketMessage } from "./RealtimeProvider";
import { DmUserSearch } from "./DmUserSearch";
import styles from "./Dm.module.css";

type Props = {
  id: string;
  initialPartner: UserSummary | null;
  /** 보고 있는 대화 상대가 바뀔 때 */
  onViewingChange: (partnerId: number | null) => void;
  /** 안 읽은 DM 수가 바뀔 때. DM 버튼 배지에 쓴다 */
  onUnreadChange: (count: number) => void;
};

type Conversation = {
  partnerId: number;
  messages: DmMessage[];
};

// 받은(또는 내가 보낸) 메시지를 대화 목록에 반영한다. 최근 대화가 맨 위로 온다.
function applyToRooms(rooms: DmRoom[], message: DmSocketMessage, partnerId: number, unread: boolean) {
  const room = rooms.find((item) => item.user.id === partnerId);
  if (!room) return rooms;
  const updated: DmRoom = {
    ...room,
    lastMessage: message.content,
    lastAt: message.createdAt,
    unreadCount: unread ? room.unreadCount + 1 : room.unreadCount,
  };
  return [updated, ...rooms.filter((item) => item !== room)];
}

/** DM 창 내용. 열릴 때 대화 목록을 받아 오고, 실시간으로 받은 메시지·읽음 표시를 반영한다. */
export function DmPanel({ id, initialPartner, onViewingChange, onUnreadChange }: Props) {
  const [myId, setMyId] = useState<number | null>(null);
  const [rooms, setRooms] = useState<DmRoom[] | null>(null);
  const [now, setNow] = useState(0);
  const [partner, setPartner] = useState(initialPartner);
  const [conversation, setConversation] = useState<Conversation | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);
  const partnerRef = useRef(partner);
  const roomsRef = useRef(rooms);

  const applyRooms = useCallback((data: Awaited<ReturnType<typeof loadDmRooms>>) => {
    setMyId(data.myId);
    setRooms(data.rooms);
    setNow(data.now);
  }, []);

  const refreshRooms = useCallback(() => {
    loadDmRooms()
      .then(applyRooms)
      .catch(() => setLoadError("대화 목록을 불러오지 못했어요."));
  }, [applyRooms]);

  useEffect(() => {
    partnerRef.current = partner;
    roomsRef.current = rooms;
  }, [partner, rooms]);

  useEffect(() => {
    onViewingChange(partner?.id ?? null);
  }, [partner, onViewingChange]);

  useEffect(() => {
    if (rooms) onUnreadChange(rooms.reduce((sum, room) => sum + room.unreadCount, 0));
  }, [rooms, onUnreadChange]);

  useEffect(() => {
    let cancelled = false;
    loadDmRooms()
      .then((data) => {
        if (!cancelled) applyRooms(data);
      })
      .catch(() => {
        if (!cancelled) setLoadError("대화 목록을 불러오지 못했어요.");
      });
    return () => {
      cancelled = true;
    };
  }, [applyRooms]);

  useEffect(() => {
    if (myId === null || !partner) return;
    let cancelled = false;
    loadDmMessages(myId, partner.id)
      .then((messages) => {
        if (cancelled) return;
        setConversation({ partnerId: partner.id, messages });
        // 대화를 열면 백엔드가 읽음 처리하므로 목록의 안 읽은 수도 지운다.
        setRooms((list) =>
          list?.map((room) => (room.user.id === partner.id ? { ...room, unreadCount: 0 } : room)) ?? list,
        );
      })
      .catch(() => {
        if (!cancelled) setLoadError("대화를 불러오지 못했어요.");
      });
    return () => {
      cancelled = true;
    };
  }, [myId, partner]);

  const realtime = useRealtime();

  const receive = useCallback(
    (message: DmSocketMessage) => {
      if (myId === null) return;
      const mine = message.senderId === myId;
      const otherId = mine ? message.receiverId : message.senderId;
      const viewing = partnerRef.current?.id === otherId;

      if (roomsRef.current?.some((room) => room.user.id === otherId)) {
        setRooms((list) => list && applyToRooms(list, message, otherId, !mine && !viewing));
      } else {
        // 처음 대화하는 상대면 목록을 다시 받아 온다.
        refreshRooms();
      }
      if (!viewing) return;
      // 보고 있는 대화에 온 메시지는 바로 읽음 처리해서 상대 화면의 "1"도 지운다.
      if (!mine) realtime.publish("/app/dm.read", { partnerId: otherId });
      setConversation((current) =>
        !current || current.partnerId !== otherId || current.messages.some((item) => item.id === message.id)
          ? current
          : {
              ...current,
              messages: [
                ...current.messages,
                {
                  id: message.id,
                  senderId: message.senderId,
                  content: message.content,
                  createdAt: message.createdAt,
                  read: false,
                },
              ],
            },
      );
    },
    [myId, refreshRooms, realtime],
  );
  useRealtimeEvent("dm", receive);

  // 상대가 내 메시지를 읽으면 그 메시지까지 "1"을 지운다.
  useRealtimeEvent("dm-read", (event) => {
    setConversation((current) =>
      !current || current.partnerId !== event.readerId
        ? current
        : {
            ...current,
            messages: current.messages.map((item) =>
              item.senderId === myId && !item.read && item.id <= event.lastReadMessageId
                ? { ...item, read: true }
                : item,
            ),
          },
    );
  });

  function handleSend(content: string): boolean {
    if (!partner || myId === null) return false;
    if (realtime.status !== "mock") {
      return realtime.publish("/app/dm.send", { receiverId: partner.id, content });
    }

    // 백엔드 없이 확인할 때는 화면에만 붙인다.
    const message: DmSocketMessage = {
      id: Date.now(),
      roomId: 0,
      senderId: myId,
      sender: null,
      receiverId: partner.id,
      content,
      createdAt: new Date().toISOString(),
    };
    receive(message);
    return true;
  }

  const room = rooms?.find((item) => item.user.id === partner?.id);
  const messages = conversation && conversation.partnerId === partner?.id ? conversation.messages : null;

  return (
    <section id={id} className={styles.layout} aria-label="DM">
      <div className={styles.side}>
        <div className={styles.sideHeader}>
          <p className={styles.roomsTitle}>메시지</p>
          <DmUserSearch rooms={rooms} myId={myId} onSelect={setPartner} />
        </div>
        {rooms ? (
          <DmRoomList rooms={rooms} now={now} activeId={partner?.id ?? null} onSelect={setPartner} />
        ) : (
          <p className={`${styles.rooms} ${styles.loading}`} role="status" aria-busy="true">
            {loadError ?? "대화 목록을 불러오는 중…"}
          </p>
        )}
      </div>
      <div className={styles.panel}>
        {partner && myId !== null ? (
          <DmChat
            key={partner.id}
            myId={myId}
            partner={room?.user ?? partner}
            online={room?.online ?? false}
            messages={messages}
            status={realtime.status}
            error={realtime.error ?? (messages ? null : loadError)}
            onSend={handleSend}
          />
        ) : (
          <div className={styles.placeholder}>
            <span className={styles.placeholderIcon} aria-hidden="true">
              💬
            </span>
            <p className={styles.placeholderTitle}>메시지</p>
            <p>왼쪽에서 대화를 고르거나, 사람 찾기에서 이름을 검색해 대화를 시작해 보세요.</p>
          </div>
        )}
      </div>
    </section>
  );
}
