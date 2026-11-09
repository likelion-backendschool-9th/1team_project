import type { Metadata } from "next";
import { Suspense } from "react";
import kit from "@/app/_components/ui/kit.module.css";
import { ContentTable } from "../_components/ContentTable";

export const metadata: Metadata = { title: "블로그 댓글 관리" };

export default function AdminPostCommentsPage({ searchParams }: PageProps<"/admin/post-comments">) {
  return (
    <>
      <h1 className={kit.pageTitle}>블로그 댓글 관리</h1>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <ContentTable kind="post-comments" noun="댓글" searchParams={searchParams} />
      </Suspense>
    </>
  );
}
