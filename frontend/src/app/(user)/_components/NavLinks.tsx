"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import {
  BookIcon,
  GridIcon,
  NewsIcon,
  TicketIcon,
} from "@/app/_components/ui/Icons";
import styles from "./NavLinks.module.css";

const items = [
  { href: "/main", label: "피드", Icon: GridIcon },
  { href: "/blog", label: "블로그", Icon: BookIcon },
  { href: "/news", label: "개발자 뉴스", Icon: NewsIcon },
  { href: null, label: "이벤트", Icon: TicketIcon },
] as const;

export function NavLinks() {
  const pathname = usePathname();

  return (
    <nav aria-label="주 메뉴">
      <ul className={styles.list}>
        {items.map(({ href, label, Icon }) => {
          const active =
            href !== null && (pathname === href || pathname.startsWith(`${href}/`));
          return (
            <li key={label}>
              {href ? (
                <Link
                  href={href}
                  className={`${styles.item} ${active ? styles.active : ""}`}
                  aria-current={active ? "page" : undefined}
                >
                  <Icon className={styles.icon} />
                  {label}
                </Link>
              ) : (
                <span className={`${styles.item} ${styles.disabled}`} aria-disabled="true">
                  <Icon className={styles.icon} />
                  {label}
                </span>
              )}
            </li>
          );
        })}
      </ul>
    </nav>
  );
}
