import type { Metadata } from "next";
import { Suspense } from "react";
import kit from "@/app/_components/ui/kit.module.css";
import { ContentTable } from "../_components/ContentTable";

export const metadata: Metadata = { title: "데모 관리" };

export default function AdminDemosPage({ searchParams }: PageProps<"/admin/demos">) {
  return (
    <>
      <h1 className={kit.pageTitle}>데모 관리</h1>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <ContentTable kind="demos" noun="데모" searchParams={searchParams} />
      </Suspense>
    </>
  );
}
