"use client";

import { useEffect, useId, useRef, useState } from "react";
import type { DemoSummary } from "@/app/_lib/types/demo";
import styles from "./DemoAttach.module.css";

type Props = {
  demos: DemoSummary[];
  defaultDemoId: string;
};

// 피드 작성창에서 내 데모 하나를 골라 첨부하는 드롭다운. 고른 값은 숨은 입력 demoId로 함께 제출된다.
export function DemoAttach({ demos, defaultDemoId }: Props) {
  const [open, setOpen] = useState(false);
  const [selectedId, setSelectedId] = useState(defaultDemoId);
  const listId = useId();
  const wrapRef = useRef<HTMLDivElement>(null);
  const buttonRef = useRef<HTMLButtonElement>(null);
  const selected = demos.find((demo) => String(demo.id) === selectedId);

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

  function choose(id: string) {
    setSelectedId(id);
    setOpen(false);
    buttonRef.current?.focus();
  }

  return (
    <div ref={wrapRef} className={styles.wrap}>
      <input type="hidden" name="demoId" value={selectedId} />
      <button
        ref={buttonRef}
        type="button"
        className={`${styles.trigger} ${selected ? styles.attached : ""}`}
        aria-expanded={open}
        aria-controls={listId}
        aria-label={selected ? `첨부한 데모: ${selected.title}. 바꾸기` : "내 데모 첨부"}
        onClick={() => setOpen((value) => !value)}
      >
        <span aria-hidden="true">🧪</span>
        <span className={styles.label}>{selected ? selected.title : "데모"}</span>
        <span className={styles.caret} aria-hidden="true">
          ▾
        </span>
      </button>
      {selected && (
        <button
          type="button"
          className={styles.clear}
          aria-label="데모 첨부 취소"
          onClick={() => choose("")}
        >
          ✕
        </button>
      )}
      <div id={listId} className={styles.panel} hidden={!open}>
        <p className={styles.panelTitle}>내 데모 첨부</p>
        {demos.length === 0 ? (
          <p className={styles.empty}>아직 올린 데모가 없어요.</p>
        ) : (
          <ul className={styles.list}>
            {demos.map((demo) => {
              const active = String(demo.id) === selectedId;
              return (
                <li key={demo.id}>
                  <button
                    type="button"
                    className={`${styles.option} ${active ? styles.optionActive : ""}`}
                    aria-pressed={active}
                    onClick={() => choose(active ? "" : String(demo.id))}
                  >
                    <span
                      className={styles.thumb}
                      style={demo.thumbnail ? { backgroundImage: `url("${demo.thumbnail}")` } : undefined}
                      aria-hidden="true"
                    >
                      {!demo.thumbnail && "🧪"}
                    </span>
                    <span className={styles.optionText}>
                      <strong>{demo.title}</strong>
                      <span>{demo.description}</span>
                    </span>
                    {active && (
                      <span className={styles.check} aria-hidden="true">
                        ✓
                      </span>
                    )}
                  </button>
                </li>
              );
            })}
          </ul>
        )}
      </div>
    </div>
  );
}
