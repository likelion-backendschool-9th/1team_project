import type { Metadata } from "next";
import { Suspense } from "react";
import { MiniHomeHome } from "./_components/MiniHomeHome";

export const metadata: Metadata = {
  title: "미니홈피",
  description: "개발자의 대표 데모, 블로그 글, 방명록을 한곳에서 둘러보세요.",
};

export default function MiniHomePage({ params }: PageProps<"/mini-home/[userId]">) {
  return (
    <>
      <h1 className="sr-only">미니홈피</h1>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <MiniHomeHome params={params} />
      </Suspense>
    </>
  );
}
