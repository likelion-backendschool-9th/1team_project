import type { ReactNode } from "react";
import { Suspense } from "react";
import { getMe } from "@/app/(user)/_lib/api";
import { MyNav } from "./_components/MyNav";

// 로그인 쿠키가 필요한 호출이라 빌드 때 미리 그릴 수 없다. Suspense 안에서 기다린다.
async function MyNavWithSession() {
  const me = await getMe();
  return <MyNav isLoggedIn={!!me} />;
}

export default function MyLayout({ children }: { children: ReactNode }) {
  return (
    <>
      <Suspense fallback={<MyNav />}>
        <MyNavWithSession />
      </Suspense>
      {children}
    </>
  );
}
