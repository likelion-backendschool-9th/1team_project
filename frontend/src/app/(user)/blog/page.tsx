import type { Metadata } from "next";
import { Suspense } from "react";
import { BlogContent } from "./_components/BlogContent";

export const metadata: Metadata = {
  title: "블로그",
  description: "시니어 개발자들의 기술 연재와 최신 기술 아티클을 읽어 보세요.",
  alternates: { canonical: "/blog" },
};

export default function BlogPage({ searchParams }: PageProps<"/blog">) {
  return (
    <>
      <h1 className="sr-only">블로그</h1>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <BlogContent searchParams={searchParams} />
      </Suspense>
    </>
  );
}
