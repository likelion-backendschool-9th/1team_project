import type { ReactNode } from "react";
import { DmWidget } from "./_components/DmWidget";

// 피드·블로그·미니홈피 등 회원 화면 전체에 DM 버튼과 DM 창을 붙인다.
export default function UserGroupLayout({ children }: { children: ReactNode }) {
  return <DmWidget>{children}</DmWidget>;
}
