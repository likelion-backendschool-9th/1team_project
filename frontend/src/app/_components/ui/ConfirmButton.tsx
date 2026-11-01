"use client";

import { useId, useRef, useState, useTransition } from "react";
import kit from "./kit.module.css";
import styles from "./ConfirmButton.module.css";

type Result = { ok: boolean; message: string };

type Props = {
  label: string;
  title: string;
  description?: string;
  confirmLabel?: string;
  className?: string;
  danger?: boolean;
  onConfirm: () => Promise<Result>;
};

// 삭제·제재처럼 되돌리기 어려운 작업 전에 확인 대화상자를 띄운다.
export function ConfirmButton({
  label,
  title,
  description,
  confirmLabel = "확인",
  className = kit.btnGhost,
  danger = false,
  onConfirm,
}: Props) {
  const titleId = useId();
  const dialogRef = useRef<HTMLDialogElement>(null);
  const triggerRef = useRef<HTMLButtonElement>(null);
  const [result, setResult] = useState<Result | null>(null);
  const [pending, startTransition] = useTransition();

  function close() {
    dialogRef.current?.close();
    triggerRef.current?.focus();
  }

  function handleConfirm() {
    startTransition(async () => {
      setResult(await onConfirm());
      close();
    });
  }

  return (
    <>
      <button
        ref={triggerRef}
        type="button"
        className={className}
        onClick={() => dialogRef.current?.showModal()}
      >
        {label}
      </button>
      <dialog
        ref={dialogRef}
        className={styles.dialog}
        aria-labelledby={titleId}
        onClose={() => triggerRef.current?.focus()}
      >
        <h2 id={titleId} className={styles.title}>
          {title}
        </h2>
        {description && <p className={styles.description}>{description}</p>}
        <div className={styles.actions}>
          <button type="button" className={kit.btn} onClick={close}>
            취소
          </button>
          <button
            type="button"
            className={danger ? kit.btnDanger : kit.btnPrimary}
            disabled={pending}
            onClick={handleConfirm}
          >
            {confirmLabel}
          </button>
        </div>
      </dialog>
      <span role="status" className={result?.ok === false ? kit.error : styles.result}>
        {result?.message}
      </span>
    </>
  );
}
