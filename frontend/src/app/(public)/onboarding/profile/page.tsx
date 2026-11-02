import type { Metadata } from "next";
import { connection } from "next/server";
import { Suspense } from "react";
import { parseRedirect } from "@/app/_lib/redirect";
import { ProfileForm } from "../_components/ProfileForm";
import { StepHeader } from "../_components/StepHeader";
import { getSocialProfile } from "../_lib/api";
import { readDraft } from "../_lib/draft";

export const metadata: Metadata = {
  title: "가입 정보 입력",
  description: "찍먹에서 쓸 닉네임을 알려 주세요.",
  robots: { index: false },
};

async function ProfileContent({ searchParams }: Pick<PageProps<"/onboarding/profile">, "searchParams">) {
  await connection();
  const [{ redirect }, social, draft] = await Promise.all([searchParams, getSocialProfile(), readDraft()]);

  return (
    <ProfileForm
      defaultNickname={draft.nickname ?? social.nickname}
      defaultBio={draft.bio ?? ""}
      defaultGithubUsername={draft.githubUsername ?? social.githubUsername}
      defaultTermsAgreed={draft.termsAgreed ?? false}
      redirectTo={parseRedirect(redirect)}
    />
  );
}

export default function OnboardingProfilePage({ searchParams }: PageProps<"/onboarding/profile">) {
  return (
    <>
      <StepHeader title="거의 다 왔어요!" desc="찍먹에서 쓸 프로필을 완성해 주세요. 닉네임만 필수예요." />
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <ProfileContent searchParams={searchParams} />
      </Suspense>
    </>
  );
}
