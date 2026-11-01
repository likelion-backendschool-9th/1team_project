import type { Metadata } from "next";
import { Suspense } from "react";
import kit from "@/app/_components/ui/kit.module.css";
import { NewsContent } from "./_components/NewsContent";

export const metadata: Metadata = {
  title: "개발자 뉴스",
  description: "오늘의 IT·개발 소식을 한눈에 확인하세요.",
};

export default function NewsPage({ searchParams }: PageProps<"/news">) {
  return (
    <>
      <header>
        <h1 className={kit.pageTitle}>개발자 뉴스</h1>
        <p className={kit.pageDesc}>네이버 뉴스에서 모은 오늘의 IT·개발 소식이에요.</p>
      </header>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <NewsContent searchParams={searchParams} />
      </Suspense>
    </>
  );
}
