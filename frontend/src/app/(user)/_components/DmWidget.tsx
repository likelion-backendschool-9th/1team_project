"use client";

import { createContext, useCallback, useContext, useEffect, useRef, useState, type ReactNode } from "react";
import { Avatar } from "@/app/_components/ui/Avatar";
import { ChatIcon, CloseIcon } from "@/app/_components/ui/Icons";
import type { UserSummary } from "@/app/_lib/types/user";
import { loadDmRooms } from "../_actions";
import { DmPanel } from "./DmPanel";
import { useRealtime, useRealtimeEvent, type DmSocketMessage } from "./RealtimeProvider";
import styles from "./DmWidget.module.css";

/** 상대를 넘기면 그 사람과의 대화를 바로 연다. */
type OpenDm = (partner?: UserSummary) => void;

const OpenDmContext = createContext<OpenDm>(() => {});

export function useOpenDm() {
  return useContext(OpenDmContext);
}

type Props = {
  children: ReactNode;
};

/** 새 DM 알림이 떠 있는 시간(ms) */
const TOAST_MS = 5000;

/**
 * 화면 오른쪽 아래의 DM 버튼과 DM 창.
 * 버튼을 누를 때마다 창이 열리고 닫힌다. 페이지를 옮겨도 창은 열린 채로 남는다.
 * 창과 버튼 바깥을 누르거나 Esc를 누르면 닫힌다.
 * 창을 닫아 둬도 안 읽은 DM 수를 버튼에 보여 주고, 새 DM이 오면 오른쪽 아래에 알림을 띄운다.
 */
export function DmWidget({ children }: Props) {
  const [open, setOpen] = useState(false);
  const rootRef = useRef<HTMLDivElement>(null);
  // 상대를 바꿔 열 때마다 창을 새로 그려 그 대화부터 보여 준다.
  const [request, setRequest] = useState<{ partner: UserSummary | null; key: number }>({
    partner: null,
    key: 0,
  });

  const { me } = useRealtime();
  const myId = me?.id ?? null;
  const [unread, setUnread] = useState(0);
  const [toast, setToast] = useState<DmSocketMessage | null>(null);
  // 창에서 보고 있는 상대. 그 사람이 보낸 DM은 알림을 띄우지 않고 안 읽은 수도 늘리지 않는다.
  const viewingRef = useRef<number | null>(null);

  const openDm = useCallback<OpenDm>((partner) => {
    setRequest((prev) => ({ partner: partner ?? null, key: partner ? prev.key + 1 : prev.key }));
    setOpen(true);
  }, []);

  const setViewing = useCallback((partnerId: number | null) => {
    viewingRef.current = partnerId;
  }, []);

  // 로그인하면 안 읽은 DM 수를 받아 온다. 창이 열려 있는 동안은 창이 알려 준다.
  useEffect(() => {
    if (myId === null) return;
    let cancelled = false;
    loadDmRooms()
      .then((data) => {
        if (!cancelled) setUnread(data.rooms.reduce((sum, room) => sum + room.unreadCount, 0));
      })
      .catch(() => {});
    return () => {
      cancelled = true;
    };
  }, [myId]);

  useEffect(() => {
    if (!open) viewingRef.current = null;
  }, [open]);

  useEffect(() => {
    if (!toast) return;
    const timer = setTimeout(() => setToast(null), TOAST_MS);
    return () => clearTimeout(timer);
  }, [toast]);

  useRealtimeEvent("dm", (message) => {
    if (myId === null || message.senderId === myId) return;
    if (open && viewingRef.current === message.senderId) return;
    if (!open) setUnread((count) => count + 1);
    if (me?.notifyDm && message.sender) setToast(message);
  });

  useEffect(() => {
    if (!open) return;
    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === "Escape") setOpen(false);
    }
    // 창·버튼 바깥을 누르면 닫는다. DM 열기 버튼([data-dm-open])은 누르는 순간 다시 열 것이므로 제외한다.
    function handlePointerDown(event: PointerEvent) {
      const target = event.target as Element | null;
      if (!target || rootRef.current?.contains(target)) return;
      if (target.closest("[data-dm-open]")) return;
      setOpen(false);
    }
    window.addEventListener("keydown", handleKeyDown);
    document.addEventListener("pointerdown", handlePointerDown);
    return () => {
      window.removeEventListener("keydown", handleKeyDown);
      document.removeEventListener("pointerdown", handlePointerDown);
    };
  }, [open]);

  return (
    <OpenDmContext value={openDm}>
      {children}
      <div ref={rootRef}>
        {open && (
          <DmPanel
            key={request.key}
            id="dm-panel"
            initialPartner={request.partner}
            onViewingChange={setViewing}
            onUnreadChange={setUnread}
          />
        )}
        {/* 화면 읽기 프로그램에도 새 DM을 알린다. */}
        <div aria-live="polite">
          {toast?.sender && (
          <button
            type="button"
            className={styles.toast}
            data-dm-open
            onClick={() => {
              const sender = toast.sender;
              setToast(null);
              if (sender) openDm(sender);
            }}
          >
            <Avatar name={toast.sender.nickname} size={40} />
            <span className={styles.toastBody}>
              <strong>{toast.sender.nickname}님의 새 메시지</strong>
              <span className={styles.toastText}>{toast.content}</span>
            </span>
          </button>
          )}
        </div>
        <button
          type="button"
          className={`${styles.button} ${open ? styles.open : ""}`}
          aria-expanded={open}
          aria-controls="dm-panel"
          aria-label={open ? "DM 닫기" : "DM 열기"}
          onClick={() => setOpen((value) => !value)}
        >
          {open ? <CloseIcon className={styles.icon} /> : <ChatIcon className={styles.icon} />}
          <span className={styles.label}>{open ? "닫기" : "DM"}</span>
          {!open && unread > 0 && (
            <span className={styles.badge}>
              {unread > 99 ? "99+" : unread}
              <span className="sr-only">개 안 읽음</span>
            </span>
          )}
        </button>
      </div>
    </OpenDmContext>
  );
}
