import type { Metadata } from "next";
import { Suspense } from "react";
import kit from "@/app/_components/ui/kit.module.css";
import { SearchContent } from "./_components/SearchContent";

export const metadata: Metadata = {
  title: "검색",
  description: "사람, 데모, 블로그 글을 한 번에 찾아보세요.",
  alternates: { canonical: "/search" },
};

export default function SearchPage({ searchParams }: PageProps<"/search">) {
  return (
    <>
      <h1 className={kit.pageTitle}>통합 검색</h1>
      <Suspense fallback={<p role="status" aria-busy="true">검색 중…</p>}>
        <SearchContent searchParams={searchParams} />
      </Suspense>
    </>
  );
}
