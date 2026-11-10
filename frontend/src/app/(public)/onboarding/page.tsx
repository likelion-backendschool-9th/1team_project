import type { Metadata } from "next";
import { connection } from "next/server";
import { Suspense } from "react";
import { parseRedirect } from "@/app/_lib/redirect";
import { OnboardingWizard } from "./_components/OnboardingWizard";
import { getSocialProfile } from "./_lib/api";

export const metadata: Metadata = {
  title: "가입 정보 입력",
  description: "찍먹에서 쓸 프로필과 관심 태그를 골라 주세요.",
  robots: { index: false },
};

// 소셜 로그인 닉네임을 초기값으로 받아 Wizard를 그린다.
async function WizardContent({ searchParams }: Pick<PageProps<"/onboarding">, "searchParams">) {
  await connection();
  const [{ redirect }, social] = await Promise.all([searchParams, getSocialProfile()]);
  return <OnboardingWizard defaultNickname={social.nickname} redirectTo={parseRedirect(redirect)} />;
}

export default function OnboardingPage({ searchParams }: PageProps<"/onboarding">) {
  return (
    <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
      <WizardContent searchParams={searchParams} />
    </Suspense>
  );
}