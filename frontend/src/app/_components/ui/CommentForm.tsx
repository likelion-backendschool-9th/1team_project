"use client";

import { useActionState, useId, type ReactNode } from "react";
import { initialFormState, type FormState } from "@/app/_lib/formState";
import { FormMessage } from "./FormMessage";
import kit from "./kit.module.css";
import styles from "./CommentForm.module.css";

type Props = {
  action: (previous: FormState, formData: FormData) => Promise<FormState>;
  label?: string;
  placeholder?: string;
  maxLength?: number;
  children?: ReactNode;
};

export function CommentForm({
  action,
  label = "댓글 쓰기",
  placeholder = "따뜻한 피드백을 남겨 주세요",
  maxLength = 300,
  children,
}: Props) {
  const [state, formAction, pending] = useActionState(action, initialFormState);
  const id = useId();
  const error = state.errors.content;

  return (
    <form action={formAction} className={styles.form}>
      <label htmlFor={id} className="sr-only">
        {label}
      </label>
      {children}
      <div className={styles.row}>
        <textarea
          key={state.attempt}
          id={id}
          name="content"
          rows={2}
          maxLength={maxLength}
          defaultValue={state.values.content}
          placeholder={placeholder}
          className={styles.input}
          aria-invalid={error ? true : undefined}
          aria-describedby={error ? `${id}-error` : undefined}
        />
        <button type="submit" className={kit.btnPrimary} disabled={pending}>
          등록
        </button>
      </div>
      <p id={`${id}-error`} className={kit.error}>
        {error}
      </p>
      <FormMessage ok={state.ok} message={state.message} />
    </form>
  );
}
