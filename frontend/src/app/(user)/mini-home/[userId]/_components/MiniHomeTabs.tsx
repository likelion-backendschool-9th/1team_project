"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import styles from "./MiniHomeFrame.module.css";

type Props = {
  userId: number;
  myId: number;
};

// "홈"은 파도타기로 남의 홈피를 구경하다가도 내 홈피로 돌아오는 탭이다. 나머지 탭은 지금 보는 홈피 기준.
export function MiniHomeTabs({ userId, myId }: Props) {
  const pathname = usePathname();
  const base = `/mini-home/${userId}`;
  const tabs = [
    { href: `/mini-home/${myId}`, label: "홈" },
    { href: `${base}/demos`, label: "데모" },
    { href: `/blog/${userId}`, label: "블로그" },
    { href: `${base}/guestbook`, label: "방명록" },
    { href: `${base}/edit`, label: "스킨" },
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
