"use client";

import Link from "next/link";
import { useActionState, useState } from "react";
import { FormMessage } from "@/app/_components/ui/FormMessage";
import kit from "@/app/_components/ui/kit.module.css";
import { initialFormState, type FormState } from "@/app/_lib/formState";

const MAX = 500;

type Props = {
  action: (previous: FormState, formData: FormData) => Promise<FormState>;
  feedId: number;
  content: string;
};

export function FeedEditForm({ action, feedId, content }: Props) {
  const [state, formAction, pending] = useActionState(action, initialFormState);
  const [length, setLength] = useState(content.length);
  const error = state.errors.content;

  return (
    <form action={formAction} className={`${kit.card} ${kit.form}`}>
      <div className={kit.between}>
        <label htmlFor="feed-edit" className={kit.cardTitle}>
          피드 수정
        </label>
        <span className={kit.faint}>
          {length} / {MAX}
        </span>
      </div>
      <textarea
        id="feed-edit"
        name="content"
        maxLength={MAX}
        defaultValue={state.values.content ?? content}
        className={kit.textarea}
        aria-invalid={error ? true : undefined}
        aria-describedby={error ? "feed-edit-error" : undefined}
        onChange={(event) => setLength(event.target.value.length)}
      />
      <p id="feed-edit-error" className={kit.error}>
        {error}
      </p>
      <div className={kit.between}>
        <FormMessage ok={state.ok} message={state.message} />
        <div className={kit.row}>
          <Link href={`/feeds/${feedId}`} className={kit.btn}>
            취소
          </Link>
          <button type="submit" className={kit.btnPrimary} disabled={pending}>
            {pending ? "저장 중…" : "수정 완료"}
          </button>
        </div>
      </div>
    </form>
  );
}
