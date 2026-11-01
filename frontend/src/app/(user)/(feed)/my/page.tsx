import type { Metadata } from "next";
import { Suspense } from "react";
import kit from "@/app/_components/ui/kit.module.css";
import { MyContent } from "./_components/MyContent";

export const metadata: Metadata = { title: "내 정보", robots: { index: false } };

export default function MyPage() {
  return (
    <>
      <h1 className={kit.pageTitle}>내 정보</h1>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <MyContent />
      </Suspense>
    </>
  );
}
