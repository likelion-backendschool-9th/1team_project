import type { Metadata } from "next";
import { Suspense } from "react";
import { FeedDetail } from "./_components/FeedDetail";

export const metadata: Metadata = { title: "피드" };

export default function FeedDetailPage({ params }: PageProps<"/feeds/[feedId]">) {
  return (
    <>
      <h1 className="sr-only">피드 상세</h1>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <FeedDetail params={params} />
      </Suspense>
    </>
  );
}
