import type { Metadata } from "next";
import { Suspense } from "react";
import { Brand } from "@/app/(user)/_components/Brand";
import { parseRedirect } from "@/app/_lib/redirect";
import { OnboardingForm } from "./_components/OnboardingForm";
import { getSocialProfile } from "./_lib/api";
import styles from "./page.module.css";

export const metadata: Metadata = {
  title: "가입 정보 입력",
  description: "찍먹에서 쓸 닉네임과 기술 스택을 알려 주세요.",
  robots: { index: false },
};

async function OnboardingContent({ searchParams }: Pick<PageProps<"/onboarding">, "searchParams">) {
  const [{ redirect }, profile] = await Promise.all([searchParams, getSocialProfile()]);

  return (
    <OnboardingForm
      defaultNickname={profile.nickname}
      defaultGithubUsername={profile.githubUsername}
      redirectTo={parseRedirect(redirect)}
    />
  );
}

export default function OnboardingPage({ searchParams }: PageProps<"/onboarding">) {
  return (
    <main id="main-content" tabIndex={-1} className={styles.page}>
      <div className={styles.inner}>
        <Brand />
        <header>
          <h1 className={styles.title}>거의 다 왔어요!</h1>
          <p className={styles.desc}>찍먹에서 쓸 프로필을 완성해 주세요. 닉네임만 필수예요.</p>
        </header>
        <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
          <OnboardingContent searchParams={searchParams} />
        </Suspense>
      </div>
    </main>
  );
}
