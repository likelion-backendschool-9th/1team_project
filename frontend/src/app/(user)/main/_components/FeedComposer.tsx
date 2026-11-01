"use client";

import { useActionState, useState } from "react";
import { ImageIcon, LinkIcon } from "@/app/_components/ui/Icons";
import { Avatar } from "@/app/_components/ui/Avatar";
import { createFeed } from "../_actions";
import { initialComposerState, MAX_FEED_LENGTH } from "../_lib/composerState";
import styles from "./FeedComposer.module.css";

type FieldProps = {
  defaultValue: string;
  error: string | null;
};

function ComposerField({ defaultValue, error }: FieldProps) {
  const [length, setLength] = useState(defaultValue.length);

  return (
    <>
      <label htmlFor="feed-content" className="sr-only">
        피드 내용
      </label>
      <input
        id="feed-content"
        name="content"
        type="text"
        defaultValue={defaultValue}
        maxLength={MAX_FEED_LENGTH}
        placeholder="무슨 소스를 개발 중인가요?"
        autoComplete="off"
        aria-invalid={error ? true : undefined}
        aria-describedby={error ? "feed-content-error" : undefined}
        className={styles.input}
        onChange={(event) => setLength(event.target.value.length)}
      />
      <span className={styles.count} aria-hidden="true">
        {length} / {MAX_FEED_LENGTH}
      </span>
    </>
  );
}

type Props = {
  nickname: string;
};

export function FeedComposer({ nickname }: Props) {
  const [state, formAction, pending] = useActionState(
    createFeed,
    initialComposerState,
  );

  return (
    <section id="feed-composer" aria-label="피드 작성" className={styles.composer}>
      <form action={formAction} className={styles.form}>
        <Avatar name={nickname} size={48} />
        <ComposerField
          key={state.attempt}
          defaultValue={state.values.content}
          error={state.error}
        />
        <button type="button" className={styles.tool} aria-label="이미지 첨부" disabled>
          <ImageIcon />
        </button>
        <button type="button" className={styles.tool} aria-label="링크 첨부" disabled>
          <LinkIcon />
        </button>
        <button type="submit" className={styles.submit} disabled={pending}>
          게시
        </button>
      </form>
      <p id="feed-content-error" role="alert" className={styles.error}>
        {state.error}
      </p>
      <p role="status" className={styles.status}>
        {state.message}
      </p>
    </section>
  );
}
