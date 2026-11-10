import type { Metadata } from "next";
import { Suspense } from "react";
import { SeriesView } from "./_components/SeriesView";

export const metadata: Metadata = { title: "연재 시리즈" };

export default function SeriesPage({ params }: PageProps<"/blog/series/[seriesId]">) {
  return (
    <>
      <h1 className="sr-only">연재 시리즈</h1>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <SeriesView params={params} />
      </Suspense>
    </>
  );
}
