"use client";

import { useActionState, useRef, useState, useSyncExternalStore } from "react";
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
  /** 임시저장 구분값. 새 글은 "new", 수정은 "edit-{글 ID}". 없으면 임시저장 버튼을 숨긴다 */
  draftKey?: string;
};

type Draft = Omit<Initial, "paid"> & { paid: boolean; savedAt: number };

// 임시저장은 아직 서버 API가 없어 이 브라우저(localStorage)에만 보관한다.
const DRAFT_EVENT = "dipsource:post-draft";

function readDraft(storageKey: string): string | null {
  try {
    return window.localStorage.getItem(storageKey);
  } catch {
    return null;
  }
}

function parseDraft(raw: string | null): Draft | null {
  if (!raw) return null;
  try {
    return JSON.parse(raw) as Draft;
  } catch {
    return null;
  }
}

function subscribeDraft(onChange: () => void) {
  window.addEventListener("storage", onChange);
  window.addEventListener(DRAFT_EVENT, onChange);
  return () => {
    window.removeEventListener("storage", onChange);
    window.removeEventListener(DRAFT_EVENT, onChange);
  };
}

// 이벤트 핸들러(임시저장 버튼)에서만 부르는 현재 시각.
function currentTime(): number {
  return Date.now();
}

function formatSavedAt(savedAt: number): string {
  return new Date(savedAt).toLocaleString("ko-KR", { month: "numeric", day: "numeric", hour: "2-digit", minute: "2-digit" });
}

const empty: Initial = { title: "", category: "", tags: "", content: "", demoId: "", price: "10", paid: false };

export function PostEditor({ action, demos, initial = empty, submitLabel, draftKey }: Props) {
  const [state, formAction, pending] = useActionState(action, initialFormState);
  const value = (key: keyof Initial) => state.values[key] ?? String(initial[key]);
  const [content, setContent] = useState(initial.content);
  const [preview, setPreview] = useState(false);
  const [paid, setPaid] = useState(initial.paid);
  const formRef = useRef<HTMLFormElement>(null);
  const storageKey = draftKey ? `dipsource:post-draft:${draftKey}` : null;
  const savedRaw = useSyncExternalStore(
    subscribeDraft,
    () => (storageKey ? readDraft(storageKey) : null),
    () => null,
  );
  const savedDraft = parseDraft(savedRaw);
  const [draftMessage, setDraftMessage] = useState<string | null>(null);
  const [restored, setRestored] = useState(false);
  const [priceOverride, setPriceOverride] = useState<string | null>(null);

  function writeDraft(next: Draft | null) {
    if (!storageKey) return;
    try {
      if (next) window.localStorage.setItem(storageKey, JSON.stringify(next));
      else window.localStorage.removeItem(storageKey);
    } catch {
      setDraftMessage("이 브라우저에서는 임시저장을 쓸 수 없어요.");
      return;
    }
    window.dispatchEvent(new Event(DRAFT_EVENT));
  }

  function saveDraft() {
    const form = formRef.current;
    if (!form) return;
    const data = new FormData(form);
    const text = (key: string) => String(data.get(key) ?? "");
    const savedAt = currentTime();
    writeDraft({
      title: text("title"),
      category: text("category"),
      tags: text("tags"),
      content,
      demoId: text("demoId"),
      price: text("price") || "10",
      paid,
      savedAt,
    });
    setRestored(true);
    setDraftMessage(`임시저장했어요 · ${formatSavedAt(savedAt)}`);
  }

  function restoreDraft() {
    const form = formRef.current;
    if (!form || !savedDraft) return;
    for (const key of ["title", "category", "tags", "demoId"] as const) {
      const field = form.elements.namedItem(key);
      if (field instanceof HTMLInputElement || field instanceof HTMLSelectElement) field.value = savedDraft[key];
    }
    setContent(savedDraft.content);
    setPaid(savedDraft.paid);
    setPriceOverride(savedDraft.price);
    setRestored(true);
    setDraftMessage(`${formatSavedAt(savedDraft.savedAt)}에 임시저장한 글을 불러왔어요.`);
  }

  function discardDraft() {
    writeDraft(null);
    setRestored(true);
    setDraftMessage("임시저장한 글을 지웠어요.");
  }

  const invalid = (key: string) =>
    state.errors[key] ? { "aria-invalid": true as const, "aria-describedby": `post-${key}-error` } : {};

  return (
    <form
      ref={formRef}
      action={formAction}
      // 발행·수정 요청을 보내면 임시저장본은 지운다. (실패해도 작성 중인 내용은 화면에 남아 있다)
      onSubmit={() => writeDraft(null)}
      className={`${kit.card} ${kit.form}`}
    >
      {savedDraft && !restored && (
        <div className={styles.draftBanner} role="status">
          <span>{formatSavedAt(savedDraft.savedAt)}에 임시저장한 글이 있어요.</span>
          <span className={kit.row}>
            <button type="button" className={`${kit.btn} ${kit.sm}`} onClick={restoreDraft}>
              불러오기
            </button>
            <button type="button" className={`${kit.btnGhost} ${kit.sm}`} onClick={discardDraft}>
              지우기
            </button>
          </span>
        </div>
      )}
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
                key={priceOverride ?? "initial"}
                id="post-price"
                name="price"
                type="number"
                min={1}
                max={100}
                defaultValue={priceOverride ?? value("price")}
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
        <div>
          <FormMessage ok={state.ok} message={state.message} />
          {draftMessage && (
            <p className={kit.faint} role="status">
              {draftMessage}
            </p>
          )}
        </div>
        <div className={kit.row}>
          {storageKey && (
            <button type="button" className={kit.btn} onClick={saveDraft} disabled={pending}>
              임시저장
            </button>
          )}
          <button type="submit" className={kit.btnPrimary} disabled={pending}>
            {pending ? "저장 중…" : submitLabel}
          </button>
        </div>
      </div>
    </form>
  );
}
