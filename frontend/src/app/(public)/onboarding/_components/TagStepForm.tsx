"use client";

import Link from "next/link";
import { useActionState, useState } from "react";
import { FormMessage } from "@/app/_components/ui/FormMessage";
import kit from "@/app/_components/ui/kit.module.css";
import { initialFormState } from "@/app/_lib/formState";
import { saveTagStep } from "../_actions";
import { MIN_TAGS, type TagStepKey } from "../_lib/steps";
import type { Tag } from "../_lib/tags";
import styles from "./TagStepForm.module.css";

type Props = {
  step: TagStepKey;
  options: Tag[];
  initial: string[];
  max: number;
  redirectTo: string;
  prevHref: string;
  isLast: boolean;
};

// 고정 태그를 체크박스로 고른다. 최대 개수에 닿으면 나머지 태그는 선택할 수 없다.
export function TagStepForm({ step, options, initial, max, redirectTo, prevHref, isLast }: Props) {
  const [state, formAction, pending] = useActionState(saveTagStep.bind(null, step), initialFormState);
  const [selected, setSelected] = useState(new Set(initial));
  const full = selected.size >= max;
  const enough = selected.size >= MIN_TAGS;

  function toggle(id: string, checked: boolean) {
    setSelected((current) => {
      const next = new Set(current);
      if (checked) next.add(id);
      else next.delete(id);
      return next;
    });
  }

  return (
    <form action={formAction} className={`${kit.card} ${kit.form}`}>
      <input type="hidden" name="redirect" value={redirectTo} />
      <fieldset className={kit.fieldset}>
        <legend className="sr-only">태그 선택</legend>
        <p className={styles.counter} role="status">
          <strong>{selected.size}</strong>/{max}개 선택
          <span className={kit.faint}> · 최소 {MIN_TAGS}개</span>
        </p>
        <ul className={styles.chips}>
          {options.map((tag) => {
            const checked = selected.has(tag.id);
            return (
              <li key={tag.id}>
                <label className={`${styles.chip} ${checked ? styles.on : ""} ${!checked && full ? styles.off : ""}`}>
                  <input
                    type="checkbox"
                    name="tags"
                    value={tag.id}
                    checked={checked}
                    disabled={!checked && full}
                    className="sr-only"
                    onChange={(event) => toggle(tag.id, event.target.checked)}
                  />
                  {tag.label}
                </label>
              </li>
            );
          })}
        </ul>
        <p id="onboarding-tags-error" className={kit.error}>
          {state.errors.tags}
        </p>
      </fieldset>

      <div className={kit.between}>
        <Link href={prevHref} className={kit.btnGhost}>
          이전
        </Link>
        <div className={styles.submit}>
          <FormMessage ok={state.ok} message={state.errors.tags ? null : state.message} />
          <button type="submit" className={kit.btnPrimary} disabled={pending || !enough}>
            {pending ? "저장 중…" : isLast ? "찍먹 시작하기" : "다음"}
          </button>
        </div>
      </div>
    </form>
  );
}
