"use client";

import { useActionState } from "react";
import { FormMessage } from "@/app/_components/ui/FormMessage";
import kit from "@/app/_components/ui/kit.module.css";
import { initialFormState } from "@/app/_lib/formState";
import { writeGuestbook } from "../../_actions";
import styles from "./Guestbook.module.css";

type Props = {
  userId: number;
};

export function GuestbookWriteForm({ userId }: Props) {
  const [state, formAction, pending] = useActionState(
    writeGuestbook.bind(null, userId),
    initialFormState,
  );
  const error = state.errors.content;

  return (
    <form action={formAction} className={styles.form}>
      <label htmlFor="guestbook-write" className={kit.label}>
        방명록 남기기
      </label>
      <textarea
        key={state.attempt}
        id="guestbook-write"
        name="content"
        defaultValue={state.values.content}
        maxLength={300}
        rows={3}
        placeholder="따뜻한 한마디를 남겨 주세요"
        className={kit.textarea}
        aria-invalid={error ? true : undefined}
        aria-describedby={error ? "guestbook-write-error" : undefined}
      />
      <p id="guestbook-write-error" className={kit.error}>
        {error}
      </p>
      <div className={kit.between}>
        <label className={kit.check}>
          <input type="checkbox" name="secret" />
          비밀글로 남기기
        </label>
        <button type="submit" className={kit.btnPrimary} disabled={pending}>
          등록
        </button>
      </div>
      <FormMessage ok={state.ok} message={state.message} />
    </form>
  );
}
