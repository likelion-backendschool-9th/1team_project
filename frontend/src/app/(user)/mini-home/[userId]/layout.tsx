import { Suspense } from "react";
import { MiniHomeFrame } from "./_components/MiniHomeFrame";

export default function MiniHomeUserLayout({
  children,
  params,
}: LayoutProps<"/mini-home/[userId]">) {
  return (
    <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
      <MiniHomeFrame params={params}>{children}</MiniHomeFrame>
    </Suspense>
  );
}
