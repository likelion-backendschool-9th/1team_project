import type { Metadata } from "next";
import { Suspense } from "react";
import kit from "@/app/_components/ui/kit.module.css";
import { ContentTable } from "../_components/ContentTable";

export const metadata: Metadata = { title: "블로그 관리" };

export default function AdminPostsPage({ searchParams }: PageProps<"/admin/posts">) {
  return (
    <>
      <h1 className={kit.pageTitle}>블로그 관리</h1>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <ContentTable kind="posts" noun="글" searchParams={searchParams} />
      </Suspense>
    </>
  );
}
