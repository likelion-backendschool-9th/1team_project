"use client";

import { useRouter } from "next/navigation";
import { ConfirmButton } from "@/app/_components/ui/ConfirmButton";
import { logout } from "../_actions";

/** 마이페이지 로그아웃 확인 버튼. 성공 후 메인으로 이동한다. */
export function LogoutConfirm() {
  const router = useRouter();

  return (
    <ConfirmButton
      label="로그아웃"
      title="로그아웃할까요?"
      confirmLabel="로그아웃"
      onConfirm={logout}
      onDone={(result) => {
        if (result.ok) {
          router.push("/");
          router.refresh();
        }
      }}
    />
  );
}
