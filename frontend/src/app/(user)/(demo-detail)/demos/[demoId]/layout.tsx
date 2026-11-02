import { notFound } from "next/navigation";
import { connection } from "next/server";
import { Suspense, type ReactNode } from "react";
import MiniHomeLayout from "@/app/(user)/mini-home/layout";
import { MiniHomeFrame } from "@/app/(user)/mini-home/[userId]/_components/MiniHomeFrame";
import { getDemo } from "@/app/(user)/(feed)/demos/_lib/api";

type Props = {
  params: Promise<{ demoId: string }>;
  children: ReactNode;
};

// 데모 상세는 URL을 그대로 두고, 데모를 올린 사람의 미니홈피 틀 안에서 보여 준다.
async function AuthorMiniHome({ params, children }: Props) {
  await connection();
  const demoId = Number((await params).demoId);
  const demo = Number.isSafeInteger(demoId) && demoId > 0 ? await getDemo(demoId) : null;
  if (!demo) notFound();

  return (
    <MiniHomeFrame params={Promise.resolve({ userId: String(demo.author.id) })}>{children}</MiniHomeFrame>
  );
}

export default function DemoDetailLayout({ children, params }: LayoutProps<"/demos/[demoId]">) {
  return (
    <MiniHomeLayout>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <AuthorMiniHome params={params}>{children}</AuthorMiniHome>
      </Suspense>
    </MiniHomeLayout>
  );
}
