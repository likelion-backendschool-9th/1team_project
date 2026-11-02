"use client";

import { useState } from "react";
import kit from "@/app/_components/ui/kit.module.css";
import { MAX_STACKS, STACK_MAX_LENGTH, suggestedStacks } from "../_lib/rules";
import styles from "./TechStackPicker.module.css";

type Props = {
  initial: string[];
  error?: string;
};

// 추천 태그를 누르거나 직접 입력해서 고른다. 제출용 값은 쉼표로 이어 hidden input에 담는다.
export function TechStackPicker({ initial, error }: Props) {
  const [selected, setSelected] = useState(initial);
  const [draft, setDraft] = useState("");
  const full = selected.length >= MAX_STACKS;

  function toggle(stack: string) {
    setSelected((current) =>
      current.some((item) => item.toLowerCase() === stack.toLowerCase())
        ? current.filter((item) => item.toLowerCase() !== stack.toLowerCase())
        : current.length >= MAX_STACKS
          ? current
          : [...current, stack],
    );
  }

  function addDraft() {
    const stack = draft.trim().replace(/,/g, "");
    if (!stack) return;
    if (!selected.some((item) => item.toLowerCase() === stack.toLowerCase())) toggle(stack);
    setDraft("");
  }

  const isSelected = (stack: string) => selected.some((item) => item.toLowerCase() === stack.toLowerCase());

  return (
    <fieldset className={kit.fieldset}>
      <legend className={kit.label}>
        기술 스택 <span className={kit.faint}>(선택, 최대 {MAX_STACKS}개)</span>
      </legend>
      <input type="hidden" name="techStacks" value={selected.join(",")} />
      <ul className={styles.chips} aria-label="추천 기술 스택">
        {suggestedStacks.map((stack) => (
          <li key={stack}>
            <button
              type="button"
              className={`${styles.chip} ${isSelected(stack) ? styles.on : ""}`}
              aria-pressed={isSelected(stack)}
              disabled={full && !isSelected(stack)}
              onClick={() => toggle(stack)}
            >
              {stack}
            </button>
          </li>
        ))}
      </ul>
      <div className={styles.addRow}>
        <label htmlFor="onboarding-stack-draft" className="sr-only">
          기술 스택 직접 입력
        </label>
        <input
          id="onboarding-stack-draft"
          value={draft}
          maxLength={STACK_MAX_LENGTH}
          placeholder="직접 입력 후 Enter"
          className={kit.input}
          disabled={full}
          onChange={(event) => setDraft(event.target.value)}
          onKeyDown={(event) => {
            if (event.key === "Enter") {
              event.preventDefault();
              addDraft();
            }
          }}
        />
        <button type="button" className={kit.btn} disabled={full || !draft.trim()} onClick={addDraft}>
          추가
        </button>
      </div>
      {selected.length > 0 && (
        <ul className={styles.chips} aria-label="선택한 기술 스택">
          {selected.map((stack) => (
            <li key={stack}>
              <button
                type="button"
                className={`${styles.chip} ${styles.on}`}
                aria-label={`${stack} 삭제`}
                onClick={() => toggle(stack)}
              >
                {stack} ×
              </button>
            </li>
          ))}
        </ul>
      )}
      <p id="onboarding-techStacks-error" className={kit.error}>
        {error}
      </p>
    </fieldset>
  );
}
