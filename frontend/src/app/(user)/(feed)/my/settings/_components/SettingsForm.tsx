"use client";

import { useActionState } from "react";
import { FormMessage } from "@/app/_components/ui/FormMessage";
import kit from "@/app/_components/ui/kit.module.css";
import { initialFormState } from "@/app/_lib/formState";
import { saveSettings } from "../../_actions";
import type { Settings } from "../../_lib/mock";
import styles from "./Settings.module.css";

const notifyItems: { key: keyof Settings["notify"]; label: string; hint: string }[] = [
  { key: "like", label: "좋아요", hint: "내 데모·피드·글에 좋아요가 눌렸을 때" },
  { key: "comment", label: "댓글", hint: "새 댓글과 피드백이 달렸을 때" },
  { key: "follow", label: "팔로우", hint: "누군가 나를 팔로우했을 때" },
  { key: "dm", label: "DM", hint: "새 메시지가 왔을 때" },
  { key: "guestbook", label: "방명록·일촌평", hint: "미니홈피에 글이 남겨졌을 때" },
];

export function SettingsForm({ settings }: { settings: Settings }) {
  const [state, formAction, pending] = useActionState(saveSettings, initialFormState);

  return (
    <form action={formAction} className={`${kit.card} ${kit.form}`}>
      <fieldset className={kit.fieldset}>
        <legend className={kit.cardTitle}>알림</legend>
        {notifyItems.map((item) => (
          <label key={item.key} className={styles.row}>
            <span>
              <span className={kit.strong}>{item.label}</span>
              <span className={kit.faint} style={{ display: "block" }}>
                {item.hint}
              </span>
            </span>
            <input
              type="checkbox"
              role="switch"
              name={`notify.${item.key}`}
              defaultChecked={settings.notify[item.key]}
              className={kit.switch}
            />
          </label>
        ))}
      </fieldset>

      <fieldset className={kit.fieldset}>
        <legend className={kit.cardTitle}>공개 범위</legend>
        <div className={kit.field}>
          <label htmlFor="settings-visibility" className={kit.label}>
            미니홈피·블로그 공개
          </label>
          <select id="settings-visibility" name="visibility" defaultValue={settings.visibility} className={kit.select}>
            <option value="public">전체 공개</option>
            <option value="followers">팔로워만</option>
            <option value="private">나만 보기</option>
          </select>
        </div>
        <label className={styles.row}>
          <span className={kit.strong}>모르는 사람의 DM 받기</span>
          <input type="checkbox" role="switch" name="allowDm" defaultChecked={settings.allowDm} className={kit.switch} />
        </label>
      </fieldset>

      <div className={kit.between}>
        <FormMessage ok={state.ok} message={state.message} />
        <button type="submit" className={kit.btnPrimary} disabled={pending}>
          {pending ? "저장 중…" : "저장하기"}
        </button>
      </div>
    </form>
  );
}
