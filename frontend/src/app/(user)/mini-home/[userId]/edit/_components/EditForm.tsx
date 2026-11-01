"use client";

import { useActionState } from "react";
import { FormMessage } from "@/app/_components/ui/FormMessage";
import kit from "@/app/_components/ui/kit.module.css";
import { initialFormState } from "@/app/_lib/formState";
import type { MiniHomeDemo, MiniHomeProfile } from "@/app/_lib/types/miniHome";
import { saveMiniHome } from "../../_actions";
import styles from "./EditForm.module.css";

const skins = [
  { value: "cream", label: "크림 (기본)", colors: ["#fdfbf7", "#ffc21a"] },
  { value: "midnight", label: "미드나잇", colors: ["#1f1b17", "#ffd04d"] },
  { value: "mint", label: "민트 소다", colors: ["#effbf5", "#3ddc97"] },
  { value: "cherry", label: "체리 블로썸", colors: ["#fff3f7", "#ff7eb6"] },
];

type Props = {
  profile: MiniHomeProfile;
  demos: MiniHomeDemo[];
};

export function EditForm({ profile, demos }: Props) {
  const [state, formAction, pending] = useActionState(saveMiniHome, initialFormState);
  const value = (key: string, fallback: string) => state.values[key] ?? fallback;
  const fieldError = (key: string) =>
    state.errors[key]
      ? { "aria-invalid": true as const, "aria-describedby": `edit-${key}-error` }
      : {};

  return (
    <form action={formAction} className={`${kit.card} ${kit.form}`} key={state.attempt}>
      <div>
        <h2 className={kit.cardTitle}>미니홈피 꾸미기</h2>
        <p className={kit.faint}>스킨, 대표 데모, 소개 문구를 바꿀 수 있어요.</p>
      </div>

      <fieldset className={kit.fieldset}>
        <legend className={kit.label}>스킨</legend>
        <div className={styles.skins}>
          {skins.map((skin) => (
            <label key={skin.value} className={styles.skin}>
              <input
                type="radio"
                name="skin"
                value={skin.value}
                defaultChecked={value("skin", "cream") === skin.value}
              />
              <span
                className={styles.swatch}
                style={{ background: `linear-gradient(135deg, ${skin.colors[0]} 55%, ${skin.colors[1]} 55%)` }}
                aria-hidden="true"
              />
              {skin.label}
            </label>
          ))}
        </div>
      </fieldset>

      <fieldset className={kit.fieldset}>
        <legend className={kit.label}>대표 데모</legend>
        <p className={kit.hint}>공개된 데모 중 하나를 홈 화면에 크게 보여 줘요.</p>
        <div className={styles.demos}>
          {demos.map((demo) => (
            <label key={demo.id} className={styles.demo}>
              <input
                type="radio"
                name="featuredDemoId"
                value={demo.id}
                defaultChecked={demo.id === profile.featuredDemoId}
              />
              <span>
                <strong>{demo.title}</strong>
                <span className={kit.faint}> · ♥ {demo.likeCount} · 실행 {demo.playCount}</span>
              </span>
            </label>
          ))}
        </div>
      </fieldset>

      <div className={kit.grid2}>
        <div className={kit.field}>
          <label htmlFor="edit-headline" className={kit.label}>
            한 줄 소개
          </label>
          <input
            id="edit-headline"
            name="headline"
            defaultValue={value("headline", profile.headline)}
            maxLength={30}
            className={kit.input}
            {...fieldError("headline")}
          />
          <p id="edit-headline-error" className={kit.error}>
            {state.errors.headline}
          </p>
        </div>
        <div className={kit.field}>
          <label htmlFor="edit-domain" className={kit.label}>
            개인 도메인
          </label>
          <input
            id="edit-domain"
            name="domain"
            defaultValue={value("domain", profile.domain ?? "")}
            placeholder="example.dev"
            className={kit.input}
          />
        </div>
      </div>

      <div className={kit.field}>
        <label htmlFor="edit-bgm" className={kit.label}>
          배경음악
        </label>
        <input
          id="edit-bgm"
          name="bgm"
          defaultValue={value("bgm", profile.bgm ?? "")}
          placeholder="아티스트 - 곡 제목"
          className={kit.input}
        />
      </div>

      <div className={kit.field}>
        <label htmlFor="edit-quote" className={kit.label}>
          오늘의 한마디
        </label>
        <textarea
          id="edit-quote"
          name="quote"
          defaultValue={value("quote", profile.quote ?? "")}
          maxLength={80}
          rows={2}
          className={kit.textarea}
          style={{ minHeight: 80 }}
          {...fieldError("quote")}
        />
        <p id="edit-quote-error" className={kit.error}>
          {state.errors.quote}
        </p>
      </div>

      <div className={kit.field}>
        <label htmlFor="edit-tags" className={kit.label}>
          스택 태그
        </label>
        <input
          id="edit-tags"
          name="stackTags"
          defaultValue={value("stackTags", profile.stackTags.join(", "))}
          className={kit.input}
          aria-describedby="edit-tags-hint"
        />
        <p id="edit-tags-hint" className={kit.hint}>
          쉼표로 구분해요. 예: React, TypeScript, WebGL
        </p>
      </div>

      <div className={kit.between}>
        <FormMessage ok={state.ok} message={state.message} />
        <button type="submit" className={kit.btnPrimary} disabled={pending}>
          {pending ? "저장 중…" : "저장하기"}
        </button>
      </div>
    </form>
  );
}
