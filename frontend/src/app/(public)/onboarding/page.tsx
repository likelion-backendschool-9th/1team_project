import { redirect } from "next/navigation";
import { connection } from "next/server";
import { Suspense } from "react";
import { parseRedirect } from "@/app/_lib/redirect";
import { readDraft } from "./_lib/draft";
import { firstIncompleteStep, stepUrl } from "./_lib/steps";

// 입구: 아직 끝나지 않은 첫 단계로 보낸다.
async function EnterOnboarding({ searchParams }: Pick<PageProps<"/onboarding">, "searchParams">): Promise<null> {
  await connection();
  const redirectTo = parseRedirect((await searchParams).redirect);
  const step = firstIncompleteStep(await readDraft()) ?? "topics";
  redirect(stepUrl(step, redirectTo));
}

export default function OnboardingPage({ searchParams }: PageProps<"/onboarding">) {
  return (
    <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
      <EnterOnboarding searchParams={searchParams} />
    </Suspense>
  );
}
