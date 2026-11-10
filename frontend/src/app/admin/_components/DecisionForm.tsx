"use client";

import { useActionState } from "react";
import { FormMessage } from "@/app/_components/ui/FormMessage";
import kit from "@/app/_components/ui/kit.module.css";
import { initialFormState, type FormState } from "@/app/_lib/formState";
import styles from "./DecisionForm.module.css";

type Props = {
  action: (previous: FormState, formData: FormData) => Promise<FormState>;
  title: string;
  choiceName: string;
  choiceLabel: string;
  choices: { value: string; label: string; danger?: boolean }[];
  reasonName: string;
  reasonLabel: string;
  submitLabel: string;
};

// 제재·신고 처리처럼 "무엇을 할지 + 사유"를 받는 관리자 폼.
export function DecisionForm({
  action,
  title,
  choiceName,
  choiceLabel,
  choices,
  reasonName,
  reasonLabel,
  submitLabel,
}: Props) {
  const [state, formAction, pending] = useActionState(action, initialFormState);
  const choiceError = state.errors[choiceName];
  const reasonError = state.errors[reasonName];

  return (
    <form action={formAction} className={`${kit.card} ${kit.form}`}>
      <h2 className={kit.cardTitle}>{title}</h2>
      <fieldset
        className={kit.fieldset}
        aria-describedby={choiceError ? `${choiceName}-error` : undefined}
      >
        <legend className={kit.label}>{choiceLabel}</legend>
        <div className={styles.choices}>
          {choices.map((choice) => (
            <label key={choice.value} className={`${styles.choice} ${choice.danger ? styles.danger : ""}`}>
              <input
                type="radio"
                name={choiceName}
                value={choice.value}
                defaultChecked={state.values[choiceName] === choice.value}
              />
              {choice.label}
            </label>
          ))}
        </div>
        <p id={`${choiceName}-error`} className={kit.error}>
          {choiceError}
        </p>
      </fieldset>
      <div className={kit.field}>
        <label htmlFor={`${reasonName}-input`} className={kit.label}>
          {reasonLabel}
        </label>
        <textarea
          id={`${reasonName}-input`}
          name={reasonName}
          rows={3}
          maxLength={500}
          defaultValue={state.values[reasonName]}
          className={kit.textarea}
          style={{ minHeight: 96 }}
          aria-invalid={reasonError ? true : undefined}
          aria-describedby={reasonError ? `${reasonName}-error` : undefined}
        />
        <p id={`${reasonName}-error`} className={kit.error}>
          {reasonError}
        </p>
      </div>
      <div className={kit.between}>
        <FormMessage ok={state.ok} message={state.message} />
        <button type="submit" className={kit.btnDark} disabled={pending}>
          {pending ? "처리 중…" : submitLabel}
        </button>
      </div>
    </form>
  );
}
