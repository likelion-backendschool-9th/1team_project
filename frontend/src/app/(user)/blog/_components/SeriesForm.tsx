"use client";

import { useActionState, useState } from "react";
import { FormMessage } from "@/app/_components/ui/FormMessage";
import kit from "@/app/_components/ui/kit.module.css";
import { initialFormState, type FormState } from "@/app/_lib/formState";

type Initial = {
  title: string;
  description: string;
  price: string;
  status: "ongoing" | "completed";
};

type Props = {
  action: (previous: FormState, formData: FormData) => Promise<FormState>;
  initial?: Initial;
  submitLabel: string;
  /** 수정할 때만 연재 상태(연재 중·완결)를 고른다 */
  editing?: boolean;
};

const empty: Initial = { title: "", description: "", price: "0", status: "ongoing" };

/** 시리즈 만들기·수정 폼. 가격은 원 단위이고 상한·하한이 없다 (0원이면 무료 시리즈) */
export function SeriesForm({ action, initial = empty, submitLabel, editing = false }: Props) {
  const [state, formAction, pending] = useActionState(action, initialFormState);
  // defaultValue만 둔 select는 폼 액션 뒤 첫 옵션으로 돌아가서 상태로 들고 있는다.
  const [status, setStatus] = useState(initial.status);
  const value = (key: keyof Initial) => state.values[key] ?? initial[key];
  const invalid = (key: string) =>
    state.errors[key] ? { "aria-invalid": true as const, "aria-describedby": `series-${key}-error` } : {};

  return (
    <form action={formAction} className={kit.form}>
      <div className={kit.field}>
        <label htmlFor="series-title" className={kit.label}>
          시리즈 제목
        </label>
        <input
          id="series-title"
          name="title"
          maxLength={80}
          required
          defaultValue={value("title")}
          className={kit.input}
          {...invalid("title")}
        />
        <p id="series-title-error" className={kit.error}>
          {state.errors.title}
        </p>
      </div>
      <div className={kit.field}>
        <label htmlFor="series-description" className={kit.label}>
          소개 (선택)
        </label>
        <textarea
          id="series-description"
          name="description"
          rows={3}
          maxLength={500}
          defaultValue={value("description")}
          className={kit.textarea}
          {...invalid("description")}
        />
        <p id="series-description-error" className={kit.error}>
          {state.errors.description}
        </p>
      </div>
      <div className={editing ? kit.grid2 : undefined}>
        <div className={kit.field}>
          <label htmlFor="series-price" className={kit.label}>
            가격 (원)
          </label>
          <input
            id="series-price"
            name="price"
            type="number"
            inputMode="numeric"
            min={0}
            step={1}
            required
            defaultValue={value("price")}
            className={kit.input}
            aria-describedby="series-price-hint"
            {...invalid("price")}
          />
          <p id="series-price-hint" className={kit.faint}>
            한 번 사면 이후 회차까지 계속 읽어요. 0원이면 무료 시리즈라 유료 회차를 넣을 수 없어요.
          </p>
          <p id="series-price-error" className={kit.error}>
            {state.errors.price}
          </p>
        </div>
        {editing && (
          <div className={kit.field}>
            <label htmlFor="series-status" className={kit.label}>
              연재 상태
            </label>
            <select
              id="series-status"
              name="status"
              value={status}
              onChange={(event) => setStatus(event.target.value === "completed" ? "completed" : "ongoing")}
              className={kit.select}
            >
              <option value="ongoing">연재 중</option>
              <option value="completed">완결</option>
            </select>
          </div>
        )}
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
