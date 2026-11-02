import type { Metadata } from "next";
import { Suspense } from "react";
import { TagStepPage } from "../_components/TagStepPage";

export const metadata: Metadata = { title: "관심주제 선택", robots: { index: false } };

export default function OnboardingTopicsPage({ searchParams }: PageProps<"/onboarding/topics">) {
  return (
    <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
      <TagStepPage step="topics" searchParams={searchParams} />
    </Suspense>
  );
}
