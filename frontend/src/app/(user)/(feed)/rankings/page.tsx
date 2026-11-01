import type { Metadata } from "next";
import { Suspense } from "react";
import kit from "@/app/_components/ui/kit.module.css";
import { RankingContent } from "./_components/RankingContent";

export const metadata: Metadata = {
  title: "랭킹",
  description: "데모, 블로그, 팔로워, 미니홈피 방문자 순위를 확인하세요.",
  alternates: { canonical: "/rankings" },
};

export default function RankingsPage({ searchParams }: PageProps<"/rankings">) {
  return (
    <>
      <header>
        <h1 className={kit.pageTitle}>찍먹 랭킹</h1>
        <p className={kit.pageDesc}>요즘 가장 사랑받는 데모와 개발자를 만나 보세요.</p>
      </header>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <RankingContent searchParams={searchParams} />
      </Suspense>
    </>
  );
}
