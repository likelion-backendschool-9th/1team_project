"use client";

import { useActionState, useState } from "react";
import { FormMessage } from "@/app/_components/ui/FormMessage";
import kit from "@/app/_components/ui/kit.module.css";
import { initialFormState, type FormState } from "@/app/_lib/formState";
import { categories } from "../_lib/query";
import { Markdown } from "./Markdown";
import styles from "./PostEditor.module.css";

type Initial = {
  title: string;
  category: string;
  tags: string;
  content: string;
  demoId: string;
  price: string;
  paid: boolean;
};

type Props = {
  action: (previous: FormState, formData: FormData) => Promise<FormState>;
  demos: { id: number; title: string }[];
  initial?: Initial;
  submitLabel: string;
};

const empty: Initial = { title: "", category: "", tags: "", content: "", demoId: "", price: "10", paid: false };

export function PostEditor({ action, demos, initial = empty, submitLabel }: Props) {
  const [state, formAction, pending] = useActionState(action, initialFormState);
  const value = (key: keyof Initial) => state.values[key] ?? String(initial[key]);
  const [content, setContent] = useState(initial.content);
  const [preview, setPreview] = useState(false);
  const [paid, setPaid] = useState(initial.paid);
  const invalid = (key: string) =>
    state.errors[key] ? { "aria-invalid": true as const, "aria-describedby": `post-${key}-error` } : {};

  return (
    <form action={formAction} className={`${kit.card} ${kit.form}`}>
      <div className={kit.field}>
        <label htmlFor="post-title" className={kit.label}>
          제목
        </label>
        <input
          id="post-title"
          name="title"
          maxLength={80}
          defaultValue={value("title")}
          placeholder="예: JPA N+1 문제를 3가지 방법으로 해결하기"
          className={`${kit.input} ${styles.title}`}
          {...invalid("title")}
        />
        <p id="post-title-error" className={kit.error}>
          {state.errors.title}
        </p>
      </div>

      <div className={kit.grid2}>
        <div className={kit.field}>
          <label htmlFor="post-category" className={kit.label}>
            분류
          </label>
          <select
            id="post-category"
            name="category"
            defaultValue={value("category")}
            className={kit.select}
            {...invalid("category")}
          >
            <option value="">분류 선택</option>
            {categories.map((item) => (
              <option key={item.value} value={item.value}>
                {item.label}
              </option>
            ))}
          </select>
          <p id="post-category-error" className={kit.error}>
            {state.errors.category}
          </p>
        </div>
        <div className={kit.field}>
          <label htmlFor="post-tags" className={kit.label}>
            태그
          </label>
          <input
            id="post-tags"
            name="tags"
            defaultValue={value("tags")}
            placeholder="Spring, JPA, 성능"
            className={kit.input}
          />
        </div>
      </div>

      <div className={kit.field}>
        <div className={kit.between}>
          <label htmlFor="post-content" className={kit.label}>
            본문 (마크다운)
          </label>
          <div className={styles.modes} role="group" aria-label="보기 방식">
            <button type="button" aria-pressed={!preview} onClick={() => setPreview(false)}>
              편집
            </button>
            <button type="button" aria-pressed={preview} onClick={() => setPreview(true)}>
              미리보기
            </button>
          </div>
        </div>
        <textarea
          id="post-content"
          name="content"
          value={content}
          onChange={(event) => setContent(event.target.value)}
          placeholder={"## 소제목\n\n본문을 마크다운으로 작성해요.\n\n- 목록\n\n```java\n코드 블록\n```"}
          className={`${kit.textarea} ${styles.content}`}
          hidden={preview}
          {...invalid("content")}
        />
        {preview && (
          <div className={styles.preview} aria-live="polite">
            {content.trim() ? <Markdown source={content} /> : <p className={kit.faint}>작성한 내용이 없어요.</p>}
          </div>
        )}
        <p id="post-content-error" className={kit.error}>
          {state.errors.content}
        </p>
      </div>

      <div className={kit.grid2}>
        <div className={kit.field}>
          <label htmlFor="post-demo" className={kit.label}>
            데모 연결 (선택)
          </label>
          <select id="post-demo" name="demoId" defaultValue={value("demoId")} className={kit.select}>
            <option value="">연결하지 않음</option>
            {demos.map((demo) => (
              <option key={demo.id} value={demo.id}>
                {demo.title}
              </option>
            ))}
          </select>
        </div>
        <fieldset className={kit.fieldset}>
          <legend className={kit.label}>공개 방식</legend>
          <label className={styles.paidRow}>
            <input
              type="checkbox"
              role="switch"
              name="paid"
              checked={paid}
              onChange={(event) => setPaid(event.target.checked)}
              className={kit.switch}
            />
            {paid ? "유료 글" : "무료 공개"}
          </label>
          {paid && (
            <div className={styles.priceRow}>
              <label htmlFor="post-price" className="sr-only">
                열람 가격 (칩)
              </label>
              <input
                id="post-price"
                name="price"
                type="number"
                min={1}
                max={100}
                defaultValue={value("price")}
                className={`${kit.input} ${styles.price}`}
                {...invalid("price")}
              />
              <span className={kit.muted}>칩</span>
            </div>
          )}
          <p id="post-price-error" className={kit.error}>
            {state.errors.price}
          </p>
        </fieldset>
      </div>

      <div className={kit.between}>
        <FormMessage ok={state.ok} message={state.message} />
        <button type="submit" className={kit.btnPrimary} disabled={pending}>
          {pending ? "저장 중…" : submitLabel}
        </button>
      </div>
    </form>
  );
}
