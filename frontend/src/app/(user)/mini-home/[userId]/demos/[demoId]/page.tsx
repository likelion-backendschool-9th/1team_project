import type { Metadata } from "next";
import { Suspense } from "react";
import { DemoDetailView } from "./_components/DemoDetailView";

export const metadata: Metadata = { title: "데모 상세" };

export default function DemoDetailPage({ params }: PageProps<"/mini-home/[userId]/demos/[demoId]">) {
  return (
    <>
      <h1 className="sr-only">데모 상세</h1>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <DemoDetailView params={params} />
      </Suspense>
    </>
  );
}
