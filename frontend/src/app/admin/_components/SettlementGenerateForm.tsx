"use client";

import { useActionState } from "react";
import { FormMessage } from "@/app/_components/ui/FormMessage";
import kit from "@/app/_components/ui/kit.module.css";
import { initialFormState } from "@/app/_lib/formState";
import { generateSettlements } from "../_actions";

type Props = {
  /** 기본으로 고를 달 (지난달). 예: 2026-10 */
  defaultPeriod: string;
  /** 고를 수 있는 마지막 달 (이번 달) */
  maxPeriod: string;
};

// 한 달 치 수익(글 판매·후원)을 모아 회원별 정산 대기 건을 만든다.
export function SettlementGenerateForm({ defaultPeriod, maxPeriod }: Props) {
  const [state, formAction, pending] = useActionState(generateSettlements, initialFormState);
  const error = state.errors.period;

  return (
    <form action={formAction} className={`${kit.card} ${kit.form}`}>
      <div className={kit.field}>
        <label htmlFor="settlement-period" className={kit.label}>
          정산 만들 달
        </label>
        <div className={kit.row}>
          <input
            id="settlement-period"
            name="period"
            type="month"
            max={maxPeriod}
            defaultValue={state.values.period ?? defaultPeriod}
            className={kit.input}
            aria-invalid={error ? true : undefined}
            aria-describedby={error ? "settlement-period-error" : undefined}
          />
          <button type="submit" className={kit.btnPrimary} disabled={pending}>
            {pending ? "만드는 중…" : "정산 만들기"}
          </button>
        </div>
        <p id="settlement-period-error" className={kit.error}>
          {error}
        </p>
      </div>
      <FormMessage ok={state.ok} message={state.message} />
    </form>
  );
}
