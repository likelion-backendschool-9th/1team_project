"use client";

import type { ReactNode } from "react";
import type { UserSummary } from "@/app/_lib/types/user";
import { useOpenDm } from "./DmWidget";

type Props = {
  user: UserSummary;
  className?: string;
  /** 아이콘만 있는 버튼일 때 읽어 줄 이름 */
  "aria-label"?: string;
  children: ReactNode;
};

/** 누르면 화면 이동 없이 DM 창에서 이 회원과의 대화를 연다. */
export function DmOpenButton({ user, className, "aria-label": ariaLabel, children }: Props) {
  const openDm = useOpenDm();
  return (
    <button type="button" className={className} data-dm-open aria-label={ariaLabel} onClick={() => openDm(user)}>
      {children}
    </button>
  );
}
