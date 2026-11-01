import type { Metadata } from "next";
import { Suspense } from "react";
import kit from "@/app/_components/ui/kit.module.css";
import { NotificationContent } from "./_components/NotificationContent";

export const metadata: Metadata = { title: "알림", robots: { index: false } };

export default function NotificationsPage({ searchParams }: PageProps<"/notifications">) {
  return (
    <>
      <header>
        <h1 className={kit.pageTitle}>알림</h1>
        <p className={kit.pageDesc}>좋아요, 댓글, 팔로우, 방명록 소식을 모아 봤어요.</p>
      </header>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <NotificationContent searchParams={searchParams} />
      </Suspense>
    </>
  );
}
