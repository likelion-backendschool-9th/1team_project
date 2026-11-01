"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { Suspense } from "react";
import styles from "./MyNav.module.css";

const items = [
  { href: "/my", label: "내 정보" },
  { href: "/my/follows", label: "팔로우" },
  { href: "/my/bookmarks", label: "북마크" },
  { href: "/my/settings", label: "설정" },
  { href: "/my/coins", label: "칩" },
  { href: "/my/subscription", label: "구독" },
];

function NavList({ pathname }: { pathname: string | null }) {
  return (
    <nav aria-label="내 정보 메뉴">
      <ul className={styles.list}>
        {items.map((item) => {
          const active = pathname === item.href;
          return (
            <li key={item.href}>
              <Link
                href={item.href}
                className={`${styles.tab} ${active ? styles.active : ""}`}
                aria-current={active ? "page" : undefined}
              >
                {item.label}
              </Link>
            </li>
          );
        })}
      </ul>
    </nav>
  );
}

function ActiveNavList() {
  return <NavList pathname={usePathname()} />;
}

export function MyNav() {
  return (
    <Suspense fallback={<NavList pathname={null} />}>
      <ActiveNavList />
    </Suspense>
  );
}
