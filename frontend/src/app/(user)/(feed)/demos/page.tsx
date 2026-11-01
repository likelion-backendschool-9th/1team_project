import type { Metadata } from "next";
import Link from "next/link";
import { Suspense } from "react";
import kit from "@/app/_components/ui/kit.module.css";
import { DemoList } from "./_components/DemoList";

export const metadata: Metadata = {
  title: "데모 탐색",
  description: "개발자들이 올린 데모를 태그와 인기순으로 찾아 바로 찍먹해 보세요.",
  alternates: { canonical: "/demos" },
};

export default function DemosPage({ searchParams }: PageProps<"/demos">) {
  return (
    <>
      <header className={kit.pageHeader}>
        <div>
          <h1 className={kit.pageTitle}>데모 탐색</h1>
          <p className={kit.pageDesc}>브라우저에서 바로 실행되는 개발자들의 소스를 찍먹해 보세요.</p>
        </div>
        <Link href="/demos/upload" className={kit.btnPrimary}>
          + 데모 올리기
        </Link>
      </header>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <DemoList searchParams={searchParams} />
      </Suspense>
    </>
  );
}
