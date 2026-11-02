"use client";

import { useActionState, useState, useTransition } from "react";
import { Avatar } from "@/app/_components/ui/Avatar";
import { FormMessage } from "@/app/_components/ui/FormMessage";
import kit from "@/app/_components/ui/kit.module.css";
import { initialFormState } from "@/app/_lib/formState";
import { checkNickname, submitOnboarding, type NicknameCheck } from "../_actions";
import { BIO_MAX, NICKNAME_MAX, parseStacks } from "../_lib/rules";
import { TechStackPicker } from "./TechStackPicker";
import styles from "./OnboardingForm.module.css";

type Props = {
  defaultNickname: string;
  defaultGithubUsername: string;
  redirectTo: string;
};

export function OnboardingForm({ defaultNickname, defaultGithubUsername, redirectTo }: Props) {
  const [state, formAction, pending] = useActionState(submitOnboarding, initialFormState);
  const [nickname, setNickname] = useState(state.values.nickname ?? defaultNickname);
  const [check, setCheck] = useState<NicknameCheck | null>(null);
  const [checking, startChecking] = useTransition();

  const nicknameMessage = check?.ok === false ? check.message : state.errors.nickname;
  const invalid = (key: string, message: string | null | undefined) =>
    message ? { "aria-invalid": true as const, "aria-describedby": `onboarding-${key}-error` } : {};

  function handleCheck() {
    startChecking(async () => setCheck(await checkNickname(nickname)));
  }

  return (
    <form action={formAction} className={`${kit.card} ${kit.form}`} noValidate>
      <input type="hidden" name="redirect" value={redirectTo} />

      <div className={styles.avatarRow}>
        <Avatar name={nickname || "?"} size={64} />
        <p className={kit.muted}>소셜 계정의 기본 정보로 시작해요. 프로필 이미지는 가입 후 마이페이지에서 바꿀 수 있어요.</p>
      </div>

      <div className={kit.field}>
        <label htmlFor="onboarding-nickname" className={kit.label}>
          닉네임 <span className={styles.required}>필수</span>
        </label>
        <div className={styles.nicknameRow}>
          <input
            id="onboarding-nickname"
            name="nickname"
            value={nickname}
            maxLength={NICKNAME_MAX}
            autoComplete="nickname"
            className={kit.input}
            {...invalid("nickname", nicknameMessage)}
            onChange={(event) => {
              setNickname(event.target.value);
              setCheck(null);
            }}
          />
          <button type="button" className={kit.btn} disabled={checking || !nickname.trim()} onClick={handleCheck}>
            {checking ? "확인 중…" : "중복 확인"}
          </button>
        </div>
        <p id="onboarding-nickname-error" className={kit.error}>
          {nicknameMessage}
        </p>
        <p role="status" className={kit.success}>
          {check?.ok ? check.message : null}
        </p>
      </div>

      <div className={kit.field}>
        <label htmlFor="onboarding-bio" className={kit.label}>
          자기소개 <span className={kit.faint}>(선택)</span>
        </label>
        <textarea
          id="onboarding-bio"
          name="bio"
          rows={3}
          maxLength={BIO_MAX}
          defaultValue={state.values.bio}
          placeholder="어떤 개발을 좋아하는지 알려 주세요"
          className={kit.textarea}
          style={{ minHeight: 96 }}
          {...invalid("bio", state.errors.bio)}
        />
        <p id="onboarding-bio-error" className={kit.error}>
          {state.errors.bio}
        </p>
      </div>

      <TechStackPicker
        key={state.attempt}
        initial={parseStacks(state.values.techStacks ?? "")}
        error={state.errors.techStacks}
      />

      <div className={kit.field}>
        <label htmlFor="onboarding-github" className={kit.label}>
          GitHub 사용자명 <span className={kit.faint}>(선택)</span>
        </label>
        <input
          id="onboarding-github"
          name="githubUsername"
          defaultValue={state.values.githubUsername ?? defaultGithubUsername}
          maxLength={39}
          autoComplete="off"
          className={kit.input}
          {...invalid("githubUsername", state.errors.githubUsername)}
        />
        <p id="onboarding-githubUsername-error" className={kit.error}>
          {state.errors.githubUsername}
        </p>
      </div>

      <div className={kit.field}>
        <label className={kit.check}>
          <input
            type="checkbox"
            name="terms"
            aria-describedby="onboarding-terms-error"
            aria-invalid={state.errors.terms ? true : undefined}
          />
          이용약관과 개인정보 처리방침에 동의해요 (필수)
        </label>
        <p id="onboarding-terms-error" className={kit.error}>
          {state.errors.terms}
        </p>
      </div>

      <div className={kit.between}>
        <FormMessage ok={state.ok} message={state.message} />
        <button type="submit" className={kit.btnPrimary} disabled={pending}>
          {pending ? "저장 중…" : "찍먹 시작하기"}
        </button>
      </div>
    </form>
  );
}
