"use client";

import type { ReactNode } from "react";
import type { UserSummary } from "@/app/_lib/types/user";
import { useOpenDm } from "./DmWidget";

type Props = {
  user: UserSummary;
  className?: string;
  children: ReactNode;
};

/** 누르면 화면 이동 없이 DM 창에서 이 회원과의 대화를 연다. */
export function DmOpenButton({ user, className, children }: Props) {
  const openDm = useOpenDm();
  return (
    <button type="button" className={className} onClick={() => openDm(user)}>
      {children}
    </button>
  );
}
