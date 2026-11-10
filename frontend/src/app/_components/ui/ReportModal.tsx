"use client";

import { useId, useRef, useState, useTransition } from "react";
import kit from "./kit.module.css";
import styles from "./ReportModal.module.css";

const CATEGORIES = [
  "스팸·도배",
  "욕설·혐오 표현",
  "개인정보 노출",
  "불법 콘텐츠",
  "기타",
] as const;

type Result = { ok: boolean; message: string };

type Props = {
  label: string;
  title: string;
  className?: string;
  onConfirm: (reason: string) => Promise<Result>;
  onDone?: (result: Result) => void;
};

export function ReportModal({ label, title, className = kit.btnGhost, onConfirm, onDone }: Props) {
  const titleId = useId();
  const radioName = useId();
  const dialogRef = useRef<HTMLDialogElement>(null);
  const triggerRef = useRef<HTMLButtonElement>(null);
  const [category, setCategory] = useState<string>(CATEGORIES[0]);
  const [detail, setDetail] = useState("");
  const [result, setResult] = useState<Result | null>(null);
  const [pending, startTransition] = useTransition();

  function open() {
    setCategory(CATEGORIES[0]);
    setDetail("");
    setResult(null);
    dialogRef.current?.showModal();
  }

  function close() {
    dialogRef.current?.close();
    triggerRef.current?.focus();
  }

  function handleConfirm() {
    const reason = `${category} - ${detail.trim()}`;
    startTransition(async () => {
      const next = await onConfirm(reason);
      if (onDone) onDone(next);
      else setResult(next);
      close();
    });
  }

  return (
    <>
      <button ref={triggerRef} type="button" className={className} onClick={open}>
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

        <div className={styles.section}>
          <p className={styles.sectionLabel}>신고 유형</p>
          <fieldset className={styles.radioList}>
            <legend className="sr-only">신고 유형 선택</legend>
            {CATEGORIES.map((cat) => (
              <label key={cat} className={styles.radioItem}>
                <input
                  type="radio"
                  name={radioName}
                  value={cat}
                  checked={category === cat}
                  onChange={() => setCategory(cat)}
                />
                {cat}
              </label>
            ))}
          </fieldset>
        </div>

        <div className={styles.section}>
          <p className={styles.sectionLabel}>상세 사유</p>
          <textarea
            className={styles.textarea}
            placeholder="구체적인 내용을 적어주세요."
            maxLength={500}
            value={detail}
            onChange={(e) => setDetail(e.target.value)}
          />
          <p className={styles.charCount}>{detail.length} / 500</p>
        </div>

        <div className={styles.actions}>
          <button type="button" className={kit.btn} onClick={close}>
            취소
          </button>
          <button
            type="button"
            className={kit.btnDanger}
            disabled={pending || !detail.trim()}
            onClick={handleConfirm}
          >
            신고
          </button>
        </div>
      </dialog>
      <span role="status" className={result?.ok === false ? kit.error : styles.result}>
        {result?.message}
      </span>
    </>
  );
}
