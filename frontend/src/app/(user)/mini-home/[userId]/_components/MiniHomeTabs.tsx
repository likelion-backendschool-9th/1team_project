"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import styles from "./MiniHomeFrame.module.css";

type Props = {
  userId: number;
};

export function MiniHomeTabs({ userId }: Props) {
  const pathname = usePathname();
  const base = `/mini-home/${userId}`;
  const tabs = [
    { href: base, label: "홈" },
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
