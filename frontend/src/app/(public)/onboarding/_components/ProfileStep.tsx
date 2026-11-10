"use client";

import { useState, useTransition, type FormEvent } from "react";
import { Avatar } from "@/app/_components/ui/Avatar";
import { FormMessage } from "@/app/_components/ui/FormMessage";
import kit from "@/app/_components/ui/kit.module.css";
import { checkNickname, type NicknameCheck } from "../_actions";
import { BIO_MAX, NICKNAME_MAX, validateNickname } from "../_lib/rules";
import type { OnboardingValues } from "../_lib/steps";
import styles from "./ProfileStep.module.css";

type ProfileValues = Pick<OnboardingValues, "nickname" | "bio" | "termsAgreed">;

type Props = {
  values: ProfileValues;
  onChange: (patch: Partial<ProfileValues>) => void;
  onNext: () => void;
  /** 마지막 저장에서 서버가 이 단계 문제로 돌려보낸 경우의 안내 */
  message: string | null;
};

// 1단계: 기본 정보. 값은 Wizard가 들고, 여기서는 그리기와 검사만 한다.
export function ProfileStep({ values, onChange, onNext, message }: Props) {
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [check, setCheck] = useState<NicknameCheck | null>(null);
  const [checking, startChecking] = useTransition();

  const nicknameMessage = check?.ok === false ? check.message : errors.nickname;
  const invalid = (key: string, error: string | null | undefined) =>
    error ? { "aria-invalid": true as const, "aria-describedby": `onboarding-${key}-error` } : {};

  function handleCheck() {
    startChecking(async () => setCheck(await checkNickname(values.nickname)));
  }

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); // 서버로 제출하지 않고 화면 안에서 다음 단계로 간다
    const next: Record<string, string> = {};
    const nicknameError = validateNickname(values.nickname.trim());
    if (nicknameError) next.nickname = nicknameError;
    if (values.bio.length > BIO_MAX) next.bio = `${BIO_MAX}자 이하로 입력해 주세요.`;
    if (!values.termsAgreed) next.terms = "이용약관과 개인정보 처리방침에 동의해 주세요.";
    setErrors(next);
    if (Object.keys(next).length > 0) return;

    // 형식이 맞으면 중복 확인까지 통과해야 다음으로 간다.
    startChecking(async () => {
      const result = await checkNickname(values.nickname);
      setCheck(result);
      if (result.ok) onNext();
    });
  }

  return (
    <form onSubmit={handleSubmit} className={`${kit.card} ${kit.form}`} noValidate>
      <div className={styles.avatarRow}>
        <Avatar name={values.nickname || "?"} size={64} />
        <p className={kit.muted}>소셜 계정의 기본 정보로 시작해요. 프로필 이미지는 가입 후 마이페이지에서 바꿀 수 있어요.</p>
      </div>

      <div className={kit.field}>
        <label htmlFor="onboarding-nickname" className={kit.label}>
          닉네임 <span className={styles.required}>필수</span>
        </label>
        <div className={styles.nicknameRow}>
          <input
            id="onboarding-nickname"
            value={values.nickname}
            maxLength={NICKNAME_MAX}
            autoComplete="nickname"
            className={kit.input}
            {...invalid("nickname", nicknameMessage)}
            onChange={(event) => {
              onChange({ nickname: event.target.value });
              setCheck(null);
            }}
          />
          <button type="button" className={kit.btn} disabled={checking || !values.nickname.trim()} onClick={handleCheck}>
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
          rows={3}
          maxLength={BIO_MAX}
          value={values.bio}
          placeholder="어떤 개발을 좋아하는지 알려 주세요"
          className={kit.textarea}
          style={{ minHeight: 96 }}
          {...invalid("bio", errors.bio)}
          onChange={(event) => onChange({ bio: event.target.value })}
        />
        <p id="onboarding-bio-error" className={kit.error}>
          {errors.bio}
        </p>
      </div>

      <div className={kit.field}>
        <label className={kit.check}>
          <input
            type="checkbox"
            checked={values.termsAgreed}
            aria-describedby="onboarding-terms-error"
            aria-invalid={errors.terms ? true : undefined}
            onChange={(event) => onChange({ termsAgreed: event.target.checked })}
          />
          이용약관과 개인정보 처리방침에 동의해요 (필수)
        </label>
        <p id="onboarding-terms-error" className={kit.error}>
          {errors.terms}
        </p>
      </div>

      <div className={kit.between}>
        <FormMessage ok={message ? false : null} message={message} />
        <button type="submit" className={kit.btnPrimary} disabled={checking}>
          {checking ? "확인 중…" : "다음"}
        </button>
      </div>
    </form>
  );
}