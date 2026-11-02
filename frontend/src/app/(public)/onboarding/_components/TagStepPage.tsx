import { redirect } from "next/navigation";
import { connection } from "next/server";
import { parseRedirect } from "@/app/_lib/redirect";
import { readDraft } from "../_lib/draft";
import { firstIncompleteStep, nextStep, previousStep, stepUrl, tagSteps, type TagStepKey } from "../_lib/steps";
import { StepHeader } from "./StepHeader";
import { TagStepForm } from "./TagStepForm";

type Props = {
  step: TagStepKey;
  searchParams: Promise<{ [key: string]: string | string[] | undefined }>;
};

// 기술 스택·관심분야·관심주제가 같이 쓰는 단계 화면. 앞 단계를 안 끝냈으면 그 단계로 보낸다.
export async function TagStepPage({ step, searchParams }: Props) {
  await connection();
  const redirectTo = parseRedirect((await searchParams).redirect);
  const draft = await readDraft();

  const missing = firstIncompleteStep(draft, step);
  if (missing) redirect(stepUrl(missing, redirectTo));

  const { title, desc, max, options } = tagSteps[step];
  return (
    <>
      <StepHeader title={title} desc={desc} />
      <TagStepForm
        key={step}
        step={step}
        options={options}
        initial={draft[step] ?? []}
        max={max}
        redirectTo={redirectTo}
        prevHref={stepUrl(previousStep(step) ?? "profile", redirectTo)}
        isLast={nextStep(step) === null}
      />
    </>
  );
}
