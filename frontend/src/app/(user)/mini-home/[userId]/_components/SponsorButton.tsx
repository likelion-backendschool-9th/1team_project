"use client";

import { useActionState, useId, useRef } from "react";
import { FormMessage } from "@/app/_components/ui/FormMessage";
import kit from "@/app/_components/ui/kit.module.css";
import { formatCount, formatWon } from "@/app/_lib/format";
import { initialFormState } from "@/app/_lib/formState";
import { chipsToWon } from "@/app/_lib/policy";
import { sponsorChips } from "../_actions";
import styles from "./MiniHomeHeader.module.css";

const PRESETS = [10, 30, 50, 100];

type Props = {
  userId: number;
  nickname: string;
  chipBalance: number;
};

// 미니홈피 칩 후원 (도토리 주기처럼 원하는 만큼 양도)
export function SponsorButton({ userId, nickname, chipBalance }: Props) {
  const [state, formAction, pending] = useActionState(sponsorChips.bind(null, userId), initialFormState);
  const dialogRef = useRef<HTMLDialogElement>(null);
  const triggerRef = useRef<HTMLButtonElement>(null);
  const amountRef = useRef<HTMLInputElement>(null);
  const titleId = useId();
  const error = state.errors.amount;

  return (
    <>
      <button ref={triggerRef} type="button" className={styles.button} onClick={() => dialogRef.current?.showModal()}>
        🪙 칩 후원
      </button>
      <dialog
        ref={dialogRef}
        className={styles.dialog}
        aria-labelledby={titleId}
        onClose={() => triggerRef.current?.focus()}
      >
        <form action={formAction} className={kit.form}>
          <div>
            <h2 id={titleId} className={styles.dialogTitle}>
              {nickname}님에게 칩 후원
            </h2>
            <p className={kit.faint}>보유 칩 {formatCount(chipBalance)}개 · 1칩 = {formatWon(chipsToWon(1))}</p>
          </div>
          <div className={kit.field}>
            <label htmlFor="sponsor-amount" className={kit.label}>
              후원할 칩
            </label>
            <div className={kit.row}>
              {PRESETS.map((value) => (
                <button
                  key={value}
                  type="button"
                  className={`${kit.btnGhost} ${kit.sm}`}
                  onClick={() => {
                    if (amountRef.current) amountRef.current.value = String(value);
                  }}
                >
                  {value}칩
                </button>
              ))}
            </div>
            <input
              ref={amountRef}
              id="sponsor-amount"
              name="amount"
              type="number"
              min={1}
              max={Math.min(chipBalance, 10_000)}
              defaultValue={state.values.amount ?? "10"}
              className={kit.input}
              aria-invalid={error ? true : undefined}
              aria-describedby={error ? "sponsor-amount-error" : undefined}
            />
            <p id="sponsor-amount-error" className={kit.error}>
              {error}
            </p>
          </div>
          <div className={kit.field}>
            <label htmlFor="sponsor-message" className={kit.label}>
              한마디 (선택)
            </label>
            <input
              id="sponsor-message"
              name="message"
              maxLength={50}
              defaultValue={state.values.message}
              placeholder="데모 잘 봤어요!"
              className={kit.input}
            />
          </div>
          <FormMessage ok={state.ok} message={state.message} />
          <div className={styles.dialogActions}>
            <button type="button" className={kit.btn} onClick={() => dialogRef.current?.close()}>
              닫기
            </button>
            <button type="submit" className={kit.btnPrimary} disabled={pending}>
              {pending ? "후원 중…" : "후원하기"}
            </button>
          </div>
        </form>
      </dialog>
    </>
  );
}
