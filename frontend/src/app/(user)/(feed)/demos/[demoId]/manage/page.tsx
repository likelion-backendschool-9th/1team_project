import type { Metadata } from "next";
import { notFound, redirect } from "next/navigation";
import { connection } from "next/server";
import { Suspense } from "react";
import kit from "@/app/_components/ui/kit.module.css";
import { getMe } from "@/app/(user)/_lib/api";
import { getDemo, getDemoUsage, getDemoVersions } from "../../_lib/api";
import { DemoManage } from "./_components/DemoManage";

export const metadata: Metadata = { title: "데모 관리", robots: { index: false } };

async function ManageContent({ params }: { params: Promise<{ demoId: string }> }) {
  await connection();
  const demoId = Number((await params).demoId);
  const [me, demo] = await Promise.all([
    getMe(),
    Number.isSafeInteger(demoId) && demoId > 0 ? getDemo(demoId) : null,
  ]);
  if (!demo) notFound();
  if (!me) redirect(`/login?redirect=/demos/${demoId}/manage`);
  // 권한의 최종 판단은 백엔드가 한다. 여기서는 남의 데모 관리 화면을 그리지 않을 뿐이다
  if (demo.author.id !== me.id) notFound();
  const [versions, usage] = await Promise.all([getDemoVersions(demoId), getDemoUsage()]);
  return <DemoManage demo={demo} versions={versions} usage={usage} />;
}

export default function DemoManagePage({ params }: PageProps<"/demos/[demoId]/manage">) {
  return (
    <>
      <header>
        <h1 className={kit.pageTitle}>데모 관리</h1>
        <p className={kit.pageDesc}>정보를 고치고, 새 파일을 올리고, 공개 상태를 바꿀 수 있어요.</p>
      </header>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <ManageContent params={params} />
      </Suspense>
    </>
  );
}
