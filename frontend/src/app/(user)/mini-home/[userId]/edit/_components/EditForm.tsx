"use client";

import Link from "next/link";
import { useActionState } from "react";
import { FormMessage } from "@/app/_components/ui/FormMessage";
import kit from "@/app/_components/ui/kit.module.css";
import { initialFormState } from "@/app/_lib/formState";
import { showcaseLimit } from "@/app/_lib/policy";
import type { MiniHomeDemo, MiniHomePost, MiniHomeProfile } from "@/app/_lib/types/miniHome";
import { saveMiniHome } from "../../_actions";
import { BGM_TRACKS } from "../../_lib/bgm";
import styles from "./EditForm.module.css";

const skins = [
  { value: "cream", label: "크림 (기본)", colors: ["#fdfbf7", "#ffc21a"] },
  { value: "midnight", label: "미드나잇", colors: ["#1f1b17", "#ffd04d"] },
  { value: "mint", label: "민트 소다", colors: ["#effbf5", "#3ddc97"] },
  { value: "cherry", label: "체리 블로썸", colors: ["#fff3f7", "#ff7eb6"] },
  { value: "sky", label: "하늘", colors: ["#f4f9ff", "#7cbcff"] },
  { value: "lavender", label: "라벤더", colors: ["#f8f5ff", "#b39cff"] },
  { value: "peach", label: "피치", colors: ["#fff7f1", "#ffaa7a"] },
  { value: "forest", label: "숲속", colors: ["#f1f8f3", "#5fae7e"] },
  { value: "sunset", label: "노을", colors: ["#fff4ee", "#ff8a65"] },
];

type Props = {
  profile: MiniHomeProfile;
  demos: MiniHomeDemo[];
  posts: MiniHomePost[];
};

export function EditForm({ profile, demos, posts }: Props) {
  const [state, formAction, pending] = useActionState(saveMiniHome, initialFormState);
  const limit = showcaseLimit(profile.plus);
  const showcase = state.values.showcaseDemoIds?.split(",").map(Number) ?? profile.showcaseDemoIds;
  const featured = Number(state.values.featuredDemoId ?? profile.featuredDemoId);
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

      <fieldset className={kit.fieldset} disabled={!profile.plus}>
        <legend className={kit.label}>
          스킨 {!profile.plus && <span className={styles.lock}>🔒 딥소스 플러스 전용</span>}
        </legend>
        <div className={styles.skins}>
          {skins.map((skin) => (
            <label key={skin.value} className={styles.skin}>
              <input
                type="radio"
                name="skin"
                value={skin.value}
                defaultChecked={value("skin", profile.skin) === skin.value}
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

      <fieldset
        className={kit.fieldset}
        aria-describedby={state.errors.showcase ? "edit-showcase-error" : "edit-showcase-hint"}
      >
        <legend className={kit.label}>미니홈피에 보여 줄 데모</legend>
        <p id="edit-showcase-hint" className={kit.hint}>
          최대 {limit}개까지 공개해요{!profile.plus && " (딥소스 플러스는 6개)"}. 대표 데모는 공개한 데모 중 하나를 골라요.
          데모가 없어도 저장할 수 있어요.
        </p>
        {demos.length === 0 && (
          <p className={kit.empty}>
            아직 올린 데모가 없어요. 데모를 넣어주세요.{" "}
            <Link href="/demos/upload" className={`${kit.btnPrimary} ${kit.sm}`}>
              데모 올리기
            </Link>
          </p>
        )}
        <div className={styles.demos}>
          {demos.map((demo) => (
            <div key={demo.id} className={styles.demo}>
              <label className={styles.demoCheck}>
                <input
                  type="checkbox"
                  name="showcaseDemoIds"
                  value={demo.id}
                  defaultChecked={showcase.includes(demo.id)}
                />
                <span>
                  <strong>{demo.title}</strong>
                  <span className={kit.faint}> · ♥ {demo.likeCount} · 실행 {demo.playCount}</span>
                </span>
              </label>
              <label className={styles.featuredPick}>
                <input type="radio" name="featuredDemoId" value={demo.id} defaultChecked={demo.id === featured} />
                대표
              </label>
            </div>
          ))}
        </div>
        <p id="edit-showcase-error" className={kit.error}>
          {state.errors.showcase}
        </p>
      </fieldset>

      <div className={kit.field}>
        <label htmlFor="edit-featured-post" className={kit.label}>
          제작기 글
        </label>
        <p id="edit-featured-post-hint" className={kit.hint}>
          대표 데모 아래 &quot;제작기 글&quot; 버튼에 연결할 내 블로그 글을 골라요.
        </p>
        <select
          id="edit-featured-post"
          name="featuredPostId"
          defaultValue={value("featuredPostId", profile.featuredPostId ? String(profile.featuredPostId) : "")}
          className={kit.select}
          aria-describedby="edit-featured-post-hint"
        >
          <option value="">선택 안 함</option>
          {posts.map((post) => (
            <option key={post.id} value={post.id}>
              {post.membership ? "🔒 " : ""}
              {post.title}
            </option>
          ))}
        </select>
        {posts.length === 0 && (
          <p className={kit.hint}>
            아직 쓴 블로그 글이 없어요. <Link href="/blog/write">글 쓰러 가기</Link>
          </p>
        )}
        <p id="edit-featured-post-error" className={kit.error}>
          {state.errors.featuredPost}
        </p>
      </div>

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
            maxLength={100}
            className={kit.input}
            {...fieldError("domain")}
          />
          <p id="edit-domain-error" className={kit.error}>
            {state.errors.domain}
          </p>
        </div>
      </div>

      <div className={kit.field}>
        <label htmlFor="edit-bgm" className={kit.label}>
          배경음악 {!profile.plus && <span className={styles.lock}>🔒 딥소스 플러스 전용</span>}
        </label>
        <select
          id="edit-bgm"
          name="bgm"
          defaultValue={value("bgm", profile.bgm ?? "")}
          disabled={!profile.plus}
          className={kit.select}
        >
          <option value="">사용 안 함</option>
          {BGM_TRACKS.map((track) => (
            <option key={track.id} value={track.id}>
              {track.title}
            </option>
          ))}
        </select>
        <p id="edit-bgm-error" className={kit.error}>
          {state.errors.bgm}
        </p>
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
