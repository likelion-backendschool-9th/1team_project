import type { Metadata } from "next";
import { Suspense } from "react";
import { TagStepPage } from "../_components/TagStepPage";

export const metadata: Metadata = { title: "기술 스택 선택", robots: { index: false } };

export default function OnboardingStackPage({ searchParams }: PageProps<"/onboarding/stack">) {
  return (
    <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
      <TagStepPage step="stack" searchParams={searchParams} />
    </Suspense>
  );
}
