import type { Metadata } from "next";
import { Suspense } from "react";
import { GuestbookContent } from "./_components/GuestbookContent";

export const metadata: Metadata = { title: "방명록" };

export default function GuestbookPage({ params }: PageProps<"/mini-home/[userId]/guestbook">) {
  return (
    <>
      <h1 className="sr-only">방명록</h1>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <GuestbookContent params={params} />
      </Suspense>
    </>
  );
}
