import { redirect } from "next/navigation";
import { connection } from "next/server";
import { Suspense } from "react";
import { getMe } from "@/app/(user)/_lib/api";

// "내 미니홈피" 바로가기. 로그인한 사람의 홈피로 보내고, 비로그인이면 로그인 후 다시 돌아오게 한다.
// 로그인 정보는 요청마다 달라서, 빌드 때 미리 그리지 않도록 Suspense 안에서 조회한다.
async function RedirectToMyHome(): Promise<null> {
  await connection();
  const me = await getMe();
  if (!me) redirect(`/login?redirect=${encodeURIComponent("/mini-home/me")}`);
  redirect(`/mini-home/${me.id}`);
}

export default function MyMiniHomePage() {
  return (
    <Suspense fallback={<p role="status" aria-busy="true">내 미니홈피로 이동하는 중…</p>}>
      <RedirectToMyHome />
    </Suspense>
  );
}
