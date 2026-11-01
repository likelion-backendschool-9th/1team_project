"use client";

import Link from "next/link";
import { useRef, useState, type KeyboardEvent } from "react";
import { formatCount } from "@/app/_lib/format";
import type { ArticleRanking } from "@/app/_lib/types/post";
import styles from "./ArticleRanking.module.css";

const tabs = [
  { key: "views", label: "조회" },
  { key: "likes", label: "좋아요" },
  { key: "follows", label: "팔로우" },
] as const;

type Props = {
  ranking: ArticleRanking;
};

export function ArticleRankingTabs({ ranking }: Props) {
  const [selected, setSelected] = useState(0);
  const tabRefs = useRef<(HTMLButtonElement | null)[]>([]);
  const active = tabs[selected];

  function handleKeyDown(event: KeyboardEvent<HTMLDivElement>) {
    const offset = event.key === "ArrowRight" ? 1 : event.key === "ArrowLeft" ? -1 : 0;
    if (!offset) return;
    event.preventDefault();
    const next = (selected + offset + tabs.length) % tabs.length;
    setSelected(next);
    tabRefs.current[next]?.focus();
  }

  return (
    <>
      <div
        role="tablist"
        aria-label="순위 기준"
        className={styles.tabs}
        onKeyDown={handleKeyDown}
      >
        {tabs.map((tab, index) => (
          <button
            key={tab.key}
            ref={(node) => {
              tabRefs.current[index] = node;
            }}
            type="button"
            role="tab"
            id={`article-tab-${tab.key}`}
            aria-selected={index === selected}
            aria-controls="article-tab-panel"
            tabIndex={index === selected ? 0 : -1}
            className={`${styles.tab} ${index === selected ? styles.activeTab : ""}`}
            onClick={() => setSelected(index)}
          >
            {tab.label}
          </button>
        ))}
      </div>
      <ol
        id="article-tab-panel"
        role="tabpanel"
        aria-labelledby={`article-tab-${active.key}`}
        className={styles.list}
      >
        {ranking[active.key].map((entry) => (
          <li key={entry.postId}>
            <Link href={`/blog/posts/${entry.postId}`} className={styles.row}>
              <span className={`${styles.rank} ${entry.rank === 1 ? styles.first : ""}`}>
                {entry.rank}
              </span>
              <span className={styles.name}>{entry.title}</span>
              <span className={styles.score}>{formatCount(entry.score)}</span>
            </Link>
          </li>
        ))}
      </ol>
    </>
  );
}
