"use client";

import type { FormEvent } from "react";
import { FormMessage } from "@/app/_components/ui/FormMessage";
import kit from "@/app/_components/ui/kit.module.css";
import { MIN_TAGS, tagSteps, validateTags, type TagStepKey } from "../_lib/steps";
import styles from "./TagStep.module.css";

type Props = {
  step: TagStepKey;
  selected: string[];
  onChange: (ids: string[]) => void;
  onPrev: () => void;
  onNext: () => void;
  isLast: boolean;
  pending: boolean;
  message: string | null;
};

// 2~4단계: 고정 태그를 체크박스로 고른다. 최대 개수에 닿으면 나머지는 선택할 수 없다.
export function TagStep({ step, selected, onChange, onPrev, onNext, isLast, pending, message }: Props) {
  const { max, options } = tagSteps[step];
  const chosen = new Set(selected);
  const full = chosen.size >= max;
  const enough = chosen.size >= MIN_TAGS;

  function toggle(id: string, checked: boolean) {
    onChange(checked ? [...selected, id] : selected.filter((item) => item !== id));
  }

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (validateTags(step, selected) === null) onNext();
  }

  return (
    <form onSubmit={handleSubmit} className={`${kit.card} ${kit.form}`}>
      <fieldset className={kit.fieldset}>
        <legend className="sr-only">태그 선택</legend>
        <p className={styles.counter} role="status">
          <strong>{chosen.size}</strong>/{max}개 선택
          <span className={kit.faint}> · 최소 {MIN_TAGS}개</span>
        </p>
        <ul className={styles.chips}>
          {options.map((tag) => {
            const checked = chosen.has(tag.id);
            return (
              <li key={tag.id}>
                <label className={`${styles.chip} ${checked ? styles.on : ""} ${!checked && full ? styles.off : ""}`}>
                  <input
                    type="checkbox"
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
      </fieldset>

      <div className={kit.between}>
        <button type="button" className={kit.btnGhost} onClick={onPrev} disabled={pending}>
          이전
        </button>
        <div className={styles.submit}>
          <FormMessage ok={message ? false : null} message={message} />
          <button type="submit" className={kit.btnPrimary} disabled={pending || !enough}>
            {pending ? "저장 중…" : isLast ? "찍먹 시작하기" : "다음"}
          </button>
        </div>
      </div>
    </form>
  );
}