import type { ReactNode } from "react";
import { getMe } from "@/app/(user)/_lib/api";
import { MyNav } from "./_components/MyNav";

export default async function MyLayout({ children }: { children: ReactNode }) {
  const me = await getMe();

  return (
    <>
      <MyNav isLoggedIn={!!me} />
      {children}
    </>
  );
}
