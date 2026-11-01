import type { Metadata } from "next";
import { Suspense } from "react";
import { Conversation } from "./_components/Conversation";

export const metadata: Metadata = { title: "대화", robots: { index: false } };

export default function DmRoomPage({ params }: PageProps<"/dm/[userId]">) {
  return (
    <>
      <h1 className="sr-only">대화</h1>
      <Suspense fallback={<p role="status" aria-busy="true">대화를 불러오는 중…</p>}>
        <Conversation params={params} />
      </Suspense>
    </>
  );
}
