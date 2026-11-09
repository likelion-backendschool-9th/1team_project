"use client";

import { createContext, useCallback, useContext, useEffect, useRef, useState, type ReactNode } from "react";
import { ChatIcon, CloseIcon } from "@/app/_components/ui/Icons";
import type { UserSummary } from "@/app/_lib/types/user";
import { DmPanel } from "./DmPanel";
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

/**
 * 화면 오른쪽 아래의 DM 버튼과 DM 창.
 * 버튼을 누를 때마다 창이 열리고 닫힌다. 페이지를 옮겨도 창은 열린 채로 남는다.
 * 창과 버튼 바깥을 누르거나 Esc를 누르면 닫힌다.
 */
export function DmWidget({ children }: Props) {
  const [open, setOpen] = useState(false);
  const rootRef = useRef<HTMLDivElement>(null);
  // 상대를 바꿔 열 때마다 창을 새로 그려 그 대화부터 보여 준다.
  const [request, setRequest] = useState<{ partner: UserSummary | null; key: number }>({
    partner: null,
    key: 0,
  });

  const openDm = useCallback<OpenDm>((partner) => {
    setRequest((prev) => ({ partner: partner ?? null, key: partner ? prev.key + 1 : prev.key }));
    setOpen(true);
  }, []);

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
        {open && <DmPanel key={request.key} id="dm-panel" initialPartner={request.partner} />}
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
        </button>
      </div>
    </OpenDmContext>
  );
}
