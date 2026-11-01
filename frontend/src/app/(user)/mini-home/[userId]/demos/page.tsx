import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { connection } from "next/server";
import { Suspense } from "react";
import { getDemos, getProfile } from "../_lib/api";
import { parseUserId } from "../_lib/parse";
import { PublicDemos } from "../_components/PublicDemos";

export const metadata: Metadata = { title: "미니홈피 데모" };

async function DemosContent({ params }: { params: Promise<{ userId: string }> }) {
  await connection();
  const userId = parseUserId((await params).userId);
  const profile = userId ? await getProfile(userId) : null;
  if (!userId || !profile) notFound();
  const demos = await getDemos(userId, 20);

  return (
    <PublicDemos
      userId={userId}
      demos={demos}
      featuredId={profile.featuredDemoId ?? undefined}
      showMore={false}
    />
  );
}

export default function MiniHomeDemosPage({ params }: PageProps<"/mini-home/[userId]/demos">) {
  return (
    <>
      <h1 className="sr-only">미니홈피 데모 목록</h1>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <DemosContent params={params} />
      </Suspense>
    </>
  );
}
