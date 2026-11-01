"use client";

import Link from "next/link";
import { useRef, useState, type KeyboardEvent } from "react";
import { Avatar } from "@/app/_components/ui/Avatar";
import { formatCount } from "@/app/_lib/format";
import type { RankingEntry, RankingPeriod } from "@/app/_lib/types/user";
import styles from "./RankingWidget.module.css";

const tabs: { key: RankingPeriod; label: string }[] = [
  { key: "daily", label: "오늘" },
  { key: "weekly", label: "이번주" },
  { key: "monthly", label: "이번달" },
];

type Props = {
  ranking: Record<RankingPeriod, RankingEntry[]>;
};

export function RankingTabs({ ranking }: Props) {
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
      <div className={styles.header}>
        <h2 id="ranking-title" className={styles.title}>
          오늘의 개발자
        </h2>
        <div
          role="tablist"
          aria-label="순위 기간"
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
              id={`ranking-tab-${tab.key}`}
              aria-selected={index === selected}
              aria-controls="ranking-tab-panel"
              tabIndex={index === selected ? 0 : -1}
              className={`${styles.tab} ${index === selected ? styles.activeTab : ""}`}
              onClick={() => setSelected(index)}
            >
              {tab.label}
            </button>
          ))}
        </div>
      </div>
      <ol
        id="ranking-tab-panel"
        role="tabpanel"
        aria-labelledby={`ranking-tab-${active.key}`}
        className={styles.list}
      >
        {ranking[active.key].map(({ rank, user, score }) => (
          <li key={user.id}>
            <Link href={`/mini-home/${user.id}`} className={styles.row}>
              <span className={`${styles.rank} ${rank === 1 ? styles.first : ""}`}>
                {rank}
              </span>
              <Avatar name={user.nickname} size={40} />
              <span className={styles.name}>{user.nickname}</span>
              <span className={styles.score}>{formatCount(score)}</span>
            </Link>
          </li>
        ))}
      </ol>
    </>
  );
}
