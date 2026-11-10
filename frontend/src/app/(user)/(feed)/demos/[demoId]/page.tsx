import { notFound, redirect } from "next/navigation";
import { connection } from "next/server";
import { Suspense } from "react";
import { getDemo } from "../_lib/api";

// 데모 상세는 작성자 미니홈피 안에서 보여 준다. 피드·블로그·검색 등에서 오는 /demos/[id] 링크를 그쪽으로 보낸다.
async function RedirectToAuthorHome({ params }: { params: Promise<{ demoId: string }> }): Promise<null> {
  await connection();
  const demoId = Number((await params).demoId);
  const demo = Number.isSafeInteger(demoId) && demoId > 0 ? await getDemo(demoId) : null;
  if (!demo) notFound();
  redirect(`/mini-home/${demo.author.id}/demos/${demo.id}`);
}

export default function DemoRedirectPage({ params }: PageProps<"/demos/[demoId]">) {
  return (
    <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
      <RedirectToAuthorHome params={params} />
    </Suspense>
  );
}
