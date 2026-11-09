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
      { href: "/admin/comments", label: "🗨️ 피드 댓글" },
      { href: "/admin/post-comments", label: "✍️ 블로그 댓글" },
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

// 최고 관리자(MASTER)에게만 보이는 메뉴. 일반 관리자에게는 메뉴 자체를 그리지 않는다.
const masterGroup = { title: "권한", items: [{ href: "/admin/roles", label: "🔑 권한 관리" }] };

function isActive(pathname: string | null, href: string) {
  if (!pathname) return false;
  if (href === "/admin" || href === "/admin/coins") return pathname === href;
  return pathname === href || pathname.startsWith(`${href}/`);
}

function NavList({ pathname, master }: { pathname: string | null; master: boolean }) {
  const visibleGroups = master ? [...groups, masterGroup] : groups;
  return (
    <nav aria-label="관리자 메뉴" className={styles.nav}>
      {visibleGroups.map((group) => (
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

function ActiveNavList({ master }: { master: boolean }) {
  return <NavList pathname={usePathname()} master={master} />;
}

export function AdminNav({ master = false }: { master?: boolean }) {
  return (
    <Suspense fallback={<NavList pathname={null} master={master} />}>
      <ActiveNavList master={master} />
    </Suspense>
  );
}
