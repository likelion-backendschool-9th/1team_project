import type { Metadata } from "next";
import { Suspense } from "react";
import { TagStepPage } from "../_components/TagStepPage";

export const metadata: Metadata = { title: "관심분야 선택", robots: { index: false } };

export default function OnboardingFieldsPage({ searchParams }: PageProps<"/onboarding/fields">) {
  return (
    <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
      <TagStepPage step="fields" searchParams={searchParams} />
    </Suspense>
  );
}
