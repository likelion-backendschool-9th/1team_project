"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { Suspense } from "react";
import {
  BookIcon,
  GridIcon,
  HomeIcon,
  NewsIcon,
  TicketIcon,
  UserIcon,
} from "@/app/_components/ui/Icons";
import { ChipIcon } from "@/app/_components/ui/ChipIcon";
import styles from "./NavLinks.module.css";

const items = [
  { href: "/", label: "피드", Icon: GridIcon },
  { href: "/my/feeds", label: "내 피드", Icon: UserIcon },
  { href: "/blog", label: "블로그", Icon: BookIcon },
  { href: "/news", label: "개발자 뉴스", Icon: NewsIcon },
  { href: "/events", label: "이벤트", Icon: TicketIcon },
  { href: "/my/coins", label: "칩 충전하기", Icon: ChipIcon },
  // 좁은 화면에서는 오른쪽 프로필 카드(내 미니홈피 바로가기)가 피드 맨 아래로 내려가므로 메뉴에도 둔다.
  { href: "/mini-home/me", label: "내 미니홈피", Icon: HomeIcon, narrowOnly: true },
] as const;

function NavList({ pathname }: { pathname: string | null }) {
  return (
    <nav aria-label="주 메뉴">
      <ul className={styles.list}>
        {items.map((item) => {
          const { href, label, Icon } = item;
          const active = pathname !== null && (pathname === href || pathname.startsWith(`${href}/`));
          return (
            <li key={label} className={"narrowOnly" in item ? styles.narrowOnly : undefined}>
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
