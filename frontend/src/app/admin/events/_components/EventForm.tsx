"use client";

import Link from "next/link";
import { useActionState } from "react";
import { FormMessage } from "@/app/_components/ui/FormMessage";
import kit from "@/app/_components/ui/kit.module.css";
import { initialFormState, type FormState } from "@/app/_lib/formState";
import type { DevEvent } from "@/app/_lib/types/event";

type Props = {
  action: (previous: FormState, formData: FormData) => Promise<FormState>;
  event: DevEvent | null;
};

export function EventForm({ action, event }: Props) {
  const [state, formAction, pending] = useActionState(action, initialFormState);
  const value = (key: string, fallback: string) => state.values[key] ?? fallback;
  const invalid = (key: string) =>
    state.errors[key] ? { "aria-invalid": true as const, "aria-describedby": `event-${key}-error` } : {};

  return (
    <form action={formAction} className={`${kit.card} ${kit.form}`}>
      <div className={kit.between}>
        <h2 className={kit.cardTitle}>{event ? `이벤트 수정 #${event.id}` : "새 이벤트 등록"}</h2>
        {event && (
          <Link href="/admin/events" className={kit.more}>
            수정 취소
          </Link>
        )}
      </div>
      <div className={kit.grid2}>
        <div className={kit.field}>
          <label htmlFor="event-title" className={kit.label}>제목</label>
          <input id="event-title" name="title" defaultValue={value("title", event?.title ?? "")} className={kit.input} {...invalid("title")} />
          <p id="event-title-error" className={kit.error}>{state.errors.title}</p>
        </div>
        <div className={kit.field}>
          <label htmlFor="event-reward" className={kit.label}>보상</label>
          <input id="event-reward" name="reward" defaultValue={value("reward", event?.reward ?? "")} placeholder="예: 1등 300칩" className={kit.input} />
        </div>
      </div>
      <div className={kit.field}>
        <label htmlFor="event-summary" className={kit.label}>한 줄 소개</label>
        <input id="event-summary" name="summary" defaultValue={value("summary", event?.summary ?? "")} className={kit.input} {...invalid("summary")} />
        <p id="event-summary-error" className={kit.error}>{state.errors.summary}</p>
      </div>
      <div className={kit.grid3}>
        <div className={kit.field}>
          <label htmlFor="event-status" className={kit.label}>상태</label>
          <select id="event-status" name="status" defaultValue={value("status", event?.status ?? "upcoming")} className={kit.select}>
            <option value="upcoming">예정</option>
            <option value="ongoing">진행 중</option>
            <option value="ended">종료</option>
          </select>
        </div>
        <div className={kit.field}>
          <label htmlFor="event-start" className={kit.label}>시작일</label>
          <input id="event-start" name="startAt" type="date" defaultValue={value("startAt", event?.startAt.slice(0, 10) ?? "")} className={kit.input} />
        </div>
        <div className={kit.field}>
          <label htmlFor="event-end" className={kit.label}>종료일</label>
          <input id="event-end" name="endAt" type="date" defaultValue={value("endAt", event?.endAt.slice(0, 10) ?? "")} className={kit.input} {...invalid("endAt")} />
          <p id="event-endAt-error" className={kit.error}>{state.errors.endAt}</p>
        </div>
      </div>
      <div className={kit.between}>
        <FormMessage ok={state.ok} message={state.message} />
        <button type="submit" className={kit.btnDark} disabled={pending}>
          {pending ? "저장 중…" : event ? "수정 저장" : "등록"}
        </button>
      </div>
    </form>
  );
}
