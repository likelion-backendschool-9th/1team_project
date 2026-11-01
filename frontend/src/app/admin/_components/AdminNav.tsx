"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { Suspense } from "react";
import styles from "./Admin.module.css";

const groups = [
  { title: null, items: [{ href: "/admin", label: "📊 대시보드" }] },
  { title: "회원", items: [{ href: "/admin/users", label: "👥 회원 관리" }] },
  {
    title: "콘텐츠",
    items: [
      { href: "/admin/demos", label: "🧪 데모" },
      { href: "/admin/posts", label: "📝 블로그" },
      { href: "/admin/feeds", label: "💬 피드" },
      { href: "/admin/comments", label: "🗨️ 댓글" },
      { href: "/admin/events", label: "🎉 이벤트" },
    ],
  },
  { title: "신고", items: [{ href: "/admin/reports", label: "🚨 신고 관리" }] },
  {
    title: "수익",
    items: [
      { href: "/admin/subscriptions", label: "⭐ 구독 현황" },
      { href: "/admin/revenue", label: "📈 매출 통계" },
      { href: "/admin/settlements", label: "🧾 정산" },
    ],
  },
  {
    title: "가상화폐",
    items: [
      { href: "/admin/coins", label: "🪙 충전·환불" },
      { href: "/admin/coins/suspicious", label: "🕵️ 부정 사용" },
    ],
  },
];

function isActive(pathname: string | null, href: string) {
  if (!pathname) return false;
  if (href === "/admin" || href === "/admin/coins") return pathname === href;
  return pathname === href || pathname.startsWith(`${href}/`);
}

function NavList({ pathname }: { pathname: string | null }) {
  return (
    <nav aria-label="관리자 메뉴" className={styles.nav}>
      {groups.map((group) => (
        <div key={group.title ?? "home"} className={styles.group}>
          {group.title && <span className={styles.groupTitle}>{group.title}</span>}
          <ul>
            {group.items.map((item) => {
              const active = isActive(pathname, item.href);
              return (
                <li key={item.href}>
                  <Link
                    href={item.href}
                    className={`${styles.navItem} ${active ? styles.navActive : ""}`}
                    aria-current={active ? "page" : undefined}
                  >
                    {item.label}
                  </Link>
                </li>
              );
            })}
          </ul>
        </div>
      ))}
    </nav>
  );
}

function ActiveNavList() {
  return <NavList pathname={usePathname()} />;
}

export function AdminNav() {
  return (
    <Suspense fallback={<NavList pathname={null} />}>
      <ActiveNavList />
    </Suspense>
  );
}
