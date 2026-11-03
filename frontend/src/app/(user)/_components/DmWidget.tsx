"use client";

import { createContext, useCallback, useContext, useEffect, useState, type ReactNode } from "react";
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
 */
export function DmWidget({ children }: Props) {
  const [open, setOpen] = useState(false);
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
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [open]);

  return (
    <OpenDmContext value={openDm}>
      {children}
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
    </OpenDmContext>
  );
}
