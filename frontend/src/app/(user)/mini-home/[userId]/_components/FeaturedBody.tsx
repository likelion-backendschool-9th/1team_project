"use client";

import Link from "next/link";
import { useEffect, useRef, useState, type ReactNode } from "react";
import styles from "./FeaturedDemo.module.css";

type Props = {
  stage: ReactNode;
  summary: ReactNode;
  actions: ReactNode;
  description: ReactNode;
  extra: ReactNode;
  /** 있으면 "큰 화면으로 실행"이 이 자리에서 키우는 대신 실제 데모 실행 화면으로 이동한다. */
  runHref?: string;
};

// 대표 데모는 새 창 없이 이 자리에서 크게 키워 실행한다. 좋아요·댓글은 키운 화면 아래에 그대로 둔다.
export function FeaturedBody({ stage, summary, actions, description, extra, runHref }: Props) {
  const [expanded, setExpanded] = useState(false);
  const bodyRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!expanded) return;
    bodyRef.current?.scrollIntoView({ behavior: "smooth", block: "start" });
    function handleKey(event: KeyboardEvent) {
      if (event.key === "Escape" && !document.querySelector("dialog[open]")) setExpanded(false);
    }
    document.addEventListener("keydown", handleKey);
    return () => document.removeEventListener("keydown", handleKey);
  }, [expanded]);

  return (
    <div ref={bodyRef} className={`${styles.body} ${expanded ? styles.expanded : ""}`}>
      <div className={styles.stage}>{stage}</div>
      <div className={styles.info}>
        <div className={styles.summary}>
          {summary}
          {runHref ? (
            <Link href={runHref} className={styles.run}>
              ▸ 큰 화면으로 실행
            </Link>
          ) : (
            <button
              type="button"
              className={styles.run}
              aria-pressed={expanded}
              onClick={() => setExpanded((value) => !value)}
            >
              {expanded ? "↙ 작은 화면으로" : "▸ 큰 화면으로 실행"}
            </button>
          )}
        </div>
        {actions}
        {description}
        {extra}
      </div>
    </div>
  );
}
