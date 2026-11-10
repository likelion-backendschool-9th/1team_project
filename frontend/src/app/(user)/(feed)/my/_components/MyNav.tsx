"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { Suspense, useRef } from "react";
import kit from "@/app/_components/ui/kit.module.css";
import loginStyles from "@/app/_components/ui/LoginModal.module.css";
import styles from "./MyNav.module.css";

const items = [
  { href: "/my", label: "내 정보" },
  { href: "/my/feeds", label: "내 피드", requireAuth: true },
  { href: "/my/reposts", label: "리포스트" },
  { href: "/my/follows", label: "팔로우" },
  { href: "/my/bookmarks", label: "북마크" },
  { href: "/my/settings", label: "설정" },
  { href: "/my/coins", label: "칩" },
  { href: "/my/subscription", label: "구독·구매" },
];

function NavList({
  pathname,
  isLoggedIn,
  onAuthRequired,
}: {
  pathname: string | null;
  isLoggedIn: boolean;
  onAuthRequired: () => void;
}) {
  return (
    <nav aria-label="내 정보 메뉴">
      <ul className={styles.list}>
        {items.map((item) => {
          const active = pathname === item.href;

          if (item.requireAuth && !isLoggedIn) {
            return (
              <li key={item.href}>
                <button
                  type="button"
                  className={`${styles.tab} ${active ? styles.active : ""}`}
                  onClick={onAuthRequired}
                >
                  {item.label}
                </button>
              </li>
            );
          }

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

function ActiveNavList({ isLoggedIn, onAuthRequired }: { isLoggedIn: boolean; onAuthRequired: () => void }) {
  return <NavList pathname={usePathname()} isLoggedIn={isLoggedIn} onAuthRequired={onAuthRequired} />;
}

export function MyNav({ isLoggedIn = false }: { isLoggedIn?: boolean }) {
  const dialogRef = useRef<HTMLDialogElement>(null);
  const pathname = usePathname();
  const redirectUrl = `/login?redirect=${encodeURIComponent(pathname)}`;

  return (
    <>
      <Suspense fallback={<NavList pathname={null} isLoggedIn={isLoggedIn} onAuthRequired={() => {}} />}>
        <ActiveNavList isLoggedIn={isLoggedIn} onAuthRequired={() => dialogRef.current?.showModal()} />
      </Suspense>

      <dialog ref={dialogRef} className={loginStyles.dialog} aria-labelledby="auth-modal-title">
        <h2 id="auth-modal-title" className={loginStyles.title}>로그인이 필요합니다</h2>
        <p className={loginStyles.description}>이 기능을 사용하려면 로그인해 주세요.</p>
        <div className={loginStyles.actions}>
          <button type="button" className={kit.btn} onClick={() => dialogRef.current?.close()}>
            닫기
          </button>
          <Link href={redirectUrl} className={kit.btnPrimary}>
            로그인하기
          </Link>
        </div>
      </dialog>
    </>
  );
}
