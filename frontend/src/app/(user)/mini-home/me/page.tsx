import { redirect } from "next/navigation";
import { connection } from "next/server";
import { Suspense } from "react";
import { getMe } from "@/app/(user)/_lib/api";

// 쿠키를 읽는 조회는 페이지 바로 아래에서 하면 빌드가 실패한다. Suspense 안에서만 실행한다.
async function RedirectToMyMiniHome(): Promise<null> {
  await connection();
  const me = await getMe();
  if (!me) redirect(`/login?redirect=${encodeURIComponent("/mini-home/me")}`);
  redirect(`/mini-home/${me.id}`);
}

// "내 미니홈피" 바로가기. 로그인한 사람의 홈피로 보내고, 비로그인이면 로그인 후 다시 돌아오게 한다.
export default function MyMiniHomePage() {
  return (
    <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
      <RedirectToMyMiniHome />
    </Suspense>
  );
}
