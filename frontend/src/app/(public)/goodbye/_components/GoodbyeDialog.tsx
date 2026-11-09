"use client";

import { useEffect, useRef } from "react";
import { LogoMark } from "@/app/_components/ui/LogoMark";
import kit from "@/app/_components/ui/kit.module.css";
import styles from "./GoodbyeDialog.module.css";

// 탈퇴 완료 안내. 닫으면 브라우저를 새로고침하며 로그인 안 된 메인으로 간다.
export function GoodbyeDialog() {
  const dialogRef = useRef<HTMLDialogElement>(null);

  useEffect(() => {
    const dialog = dialogRef.current;
    // 개발 모드에서는 effect가 두 번 돌아서, 이미 열려 있으면 다시 열지 않는다.
    if (dialog && !dialog.open) dialog.showModal();
  }, []);

  function handleClose() {
    window.location.replace("/main");
  }

  return (
    <dialog
      ref={dialogRef}
      className={styles.dialog}
      aria-labelledby="goodbye-title"
      aria-describedby="goodbye-desc"
      onCancel={handleClose}
    >
      <LogoMark size={56} className={styles.logo} />
      <h1 id="goodbye-title" className={styles.title}>
        회원 탈퇴가 완료되었어요
      </h1>
      <p id="goodbye-desc" className={styles.description}>
        그동안 딥소스를 이용해 주셔서 감사합니다.
        <br />
        언제든 다시 찍먹하러 와 주세요.
      </p>
      <button type="button" className={`${kit.btnPrimary} ${styles.button}`} onClick={handleClose} autoFocus>
        메인으로 가기
      </button>
    </dialog>
  );
}