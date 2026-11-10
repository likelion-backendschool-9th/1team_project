"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import styles from "./MiniHomeFrame.module.css";

type Props = {
  userId: number;
  myId?: number;
};

// 모든 탭은 지금 보고 있는 홈피 기준이다. "설정"(스킨·대표 데모·소개 꾸미기)은 내 홈피에서만 보인다.
export function MiniHomeTabs({ userId, myId }: Props) {
  const pathname = usePathname();
  const base = `/mini-home/${userId}`;
  const isOwner = myId !== undefined && myId === userId;
  const tabs = [
    { href: base, label: "홈" },
    { href: `${base}/demos`, label: "데모" },
    { href: `${base}/blog`, label: "블로그" },
    { href: `${base}/guestbook`, label: "방명록" },
    ...(isOwner ? [{ href: `${base}/edit`, label: "설정" }] : []),
  ];

  return (
    <nav aria-label="미니홈피 메뉴" className={styles.tabs}>
      <ul>
        {tabs.map((tab) => {
          const active = pathname === tab.href;
          return (
            <li key={tab.label}>
              <Link
                href={tab.href}
                className={`${styles.tab} ${active ? styles.activeTab : ""}`}
                aria-current={active ? "page" : undefined}
              >
                {tab.label}
              </Link>
            </li>
          );
        })}
      </ul>
    </nav>
  );
}
