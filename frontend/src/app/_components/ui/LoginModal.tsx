"use client";

import { useRef } from "react";
import { usePathname } from "next/navigation";
import Link from "next/link";
import kit from "./kit.module.css";
import styles from "./LoginModal.module.css";

type Props = {
  /** 모달에 보여줄 안내 문구 */
  message?: string;
};

// 비로그인 상태에서 댓글 입력을 시도하면 로그인 유도 모달을 띄운다
export function LoginModal({ message = "로그인하고 댓글을 남겨 보세요" }: Props) {
  const dialogRef = useRef<HTMLDialogElement>(null);
  const pathname = usePathname();
  const href = `/login?redirect=${encodeURIComponent(pathname)}`;

  return (
    <>
      <div className={styles.row}>
        <div
          role="button"
          tabIndex={0}
          className={styles.fakeInput}
          onClick={() => dialogRef.current?.showModal()}
          onKeyDown={(e) => {
            if (e.key === "Enter" || e.key === " ") dialogRef.current?.showModal();
          }}
        >
          따뜻한 피드백을 남겨 주세요
        </div>
        <button type="button" className={kit.btnPrimary} onClick={() => dialogRef.current?.showModal()}>
          등록
        </button>
      </div>

      <dialog ref={dialogRef} className={styles.dialog} aria-labelledby="login-modal-title">
        <h2 id="login-modal-title" className={styles.title}>로그인이 필요합니다</h2>
        <p className={styles.description}>{message}</p>
        <div className={styles.actions}>
          <button type="button" className={kit.btn} onClick={() => dialogRef.current?.close()}>
            닫기
          </button>
          <Link href={href} className={kit.btnPrimary}>
            로그인하기
          </Link>
        </div>
      </dialog>
    </>
  );
}
