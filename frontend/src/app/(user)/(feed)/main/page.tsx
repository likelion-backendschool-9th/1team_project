import type { Metadata } from "next";
import { Suspense } from "react";
import { MainContent } from "./_components/MainContent";

export const metadata: Metadata = {
  title: "피드",
  description: "개발자들이 공유한 데모와 피드를 둘러보세요.",
};

export default function MainPage({ searchParams }: PageProps<"/main">) {
  return (
    <>
      <h1 className="sr-only">피드</h1>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <MainContent searchParams={searchParams} />
      </Suspense>
    </>
  );
}
