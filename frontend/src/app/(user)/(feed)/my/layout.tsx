import type { ReactNode } from "react";
import { MyNav } from "./_components/MyNav";

export default function MyLayout({ children }: { children: ReactNode }) {
  return (
    <>
      <MyNav />
      {children}
    </>
  );
}
