"use client";

import Link from "next/link";
import { startTransition, useActionState, useState } from "react";
import { FormMessage } from "@/app/_components/ui/FormMessage";
import kit from "@/app/_components/ui/kit.module.css";
import { CUSTOM_CTA, EVENT_CTA_PRESETS } from "@/app/_lib/eventCta";
import { initialFormState, type FormState } from "@/app/_lib/formState";
import type { DevEvent } from "@/app/_lib/types/event";
import { EventImageField } from "./EventImageField";

type Props = {
  action: (previous: FormState, formData: FormData) => Promise<FormState>;
  event: DevEvent | null;
};

export function EventForm({ action, event }: Props) {
  const [state, formAction, pending] = useActionState(action, initialFormState);
  const value = (key: string, fallback: string) => state.values[key] ?? fallback;
  const invalid = (key: string) =>
    state.errors[key] ? { "aria-invalid": true as const, "aria-describedby": `event-${key}-error` } : {};
  const savedHref = event?.ctaHref ?? EVENT_CTA_PRESETS[0].href;
  const savedPreset = EVENT_CTA_PRESETS.some((preset) => preset.href === savedHref) ? savedHref : CUSTOM_CTA;
  const [ctaPreset, setCtaPreset] = useState<string>(savedPreset);
  const [imageUrl, setImageUrl] = useState(event?.imageUrl ?? "");
  const [uploading, setUploading] = useState(false);

  return (
    // <form action>으로 보내면 React 19가 끝난 뒤 폼을 초기화해서 고른 select가 첫 옵션으로 돌아간다.
    // 제출을 직접 넘겨 초기화 없이 입력값을 그대로 둔다.
    <form
      onSubmit={(e) => {
        e.preventDefault();
        const formData = new FormData(e.currentTarget);
        startTransition(() => formAction(formData));
      }}
      className={`${kit.card} ${kit.form}`}
    >
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
      <EventImageField imageUrl={imageUrl} onChange={setImageUrl} onUploadingChange={setUploading} serverError={state.errors.image} />
      <div className={kit.grid2}>
        <div className={kit.field}>
          <label htmlFor="event-cta" className={kit.label}>참여하기 버튼 이동</label>
          <select
            id="event-cta"
            name="ctaPreset"
            value={ctaPreset}
            onChange={(e) => setCtaPreset(e.target.value)}
            className={kit.select}
          >
            {EVENT_CTA_PRESETS.map((preset) => (
              <option key={preset.href} value={preset.href}>
                {preset.label} ({preset.href})
              </option>
            ))}
            <option value={CUSTOM_CTA}>직접 입력</option>
          </select>
        </div>
        {ctaPreset === CUSTOM_CTA && (
          <div className={kit.field}>
            <label htmlFor="event-cta-custom" className={kit.label}>이동할 경로</label>
            <input
              id="event-cta-custom"
              name="ctaCustom"
              defaultValue={value("ctaCustom", savedPreset === CUSTOM_CTA ? savedHref : "")}
              placeholder="예: /blog/series/12"
              className={kit.input}
              {...invalid("ctaHref")}
            />
            <p id="event-ctaHref-error" className={kit.error}>{state.errors.ctaHref}</p>
          </div>
        )}
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
        <button type="submit" className={kit.btnDark} disabled={pending || uploading}>
          {pending ? "저장 중…" : event ? "수정 저장" : "등록"}
        </button>
      </div>
    </form>
  );
}
