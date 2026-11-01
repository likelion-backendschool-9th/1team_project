"use client";

import { useActionState } from "react";
import { writeComment } from "../_actions";
import { initialCommentState, MAX_COMMENT_LENGTH } from "../_lib/commentState";
import styles from "./Board.module.css";

type Props = {
  userId: number;
};

export function GuestbookForm({ userId }: Props) {
  const [state, formAction, pending] = useActionState(
    writeComment.bind(null, userId),
    initialCommentState,
  );

  return (
    <>
      <form action={formAction} className={styles.form}>
        <label htmlFor="guestbook-content" className="sr-only">
          일촌평
        </label>
        <input
          key={state.attempt}
          id="guestbook-content"
          name="content"
          type="text"
          defaultValue={state.values.content}
          maxLength={MAX_COMMENT_LENGTH}
          placeholder="일촌평 한마디 남기기..."
          autoComplete="off"
          aria-invalid={state.error ? true : undefined}
          aria-describedby={state.error ? "guestbook-error" : undefined}
          className={styles.input}
        />
        <button type="submit" className={styles.submit} disabled={pending}>
          등록
        </button>
      </form>
      <p id="guestbook-error" role="alert" className={styles.error}>
        {state.error}
      </p>
      <p role="status" className={styles.status}>
        {state.message}
      </p>
    </>
  );
}
