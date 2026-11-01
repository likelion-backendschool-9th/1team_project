import type { Metadata } from "next";
import { Suspense } from "react";
import { EditContent } from "./_components/EditContent";

export const metadata: Metadata = { title: "미니홈피 꾸미기", robots: { index: false } };

export default function MiniHomeEditPage({ params }: PageProps<"/mini-home/[userId]/edit">) {
  return (
    <>
      <h1 className="sr-only">미니홈피 꾸미기</h1>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <EditContent params={params} />
      </Suspense>
    </>
  );
}
