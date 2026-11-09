"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { Suspense } from "react";
import {
  BookIcon,
  GridIcon,
  NewsIcon,
  TicketIcon,
  UserIcon,
} from "@/app/_components/ui/Icons";
import { ChipIcon } from "@/app/_components/ui/ChipIcon";
import styles from "./NavLinks.module.css";

const items = [
  { href: "/main", label: "피드", Icon: GridIcon },
  { href: "/my/feeds", label: "내 피드", Icon: UserIcon },
  { href: "/blog", label: "블로그", Icon: BookIcon },
  { href: "/news", label: "개발자 뉴스", Icon: NewsIcon },
  { href: "/events", label: "이벤트", Icon: TicketIcon },
  { href: "/my/coins", label: "칩 충전하기", Icon: ChipIcon },
] as const;

function NavList({ pathname }: { pathname: string | null }) {
  return (
    <nav aria-label="주 메뉴">
      <ul className={styles.list}>
        {items.map(({ href, label, Icon }) => {
          const active = pathname !== null && (pathname === href || pathname.startsWith(`${href}/`));
          return (
            <li key={label}>
              <Link
                href={href}
                className={`${styles.item} ${active ? styles.active : ""}`}
                aria-current={active ? "page" : undefined}
              >
                <Icon className={styles.icon} />
                {label}
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

// 동적 경로에서는 주소를 런타임에만 알 수 있어, 먼저 표시 없는 메뉴를 그리고 현재 위치를 이어서 채운다.
export function NavLinks() {
  return (
    <Suspense fallback={<NavList pathname={null} />}>
      <ActiveNavList />
    </Suspense>
  );
}
