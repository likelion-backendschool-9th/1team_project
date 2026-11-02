"use client";

import { useActionState } from "react";
import { FormMessage } from "@/app/_components/ui/FormMessage";
import kit from "@/app/_components/ui/kit.module.css";
import { initialFormState } from "@/app/_lib/formState";
import { saveAccount } from "../_actions";
import type { MyAccount } from "../_lib/mock";

type Props = {
  nickname: string;
  techStacks: string;
  account: MyAccount;
};

export function AccountForm({ nickname, techStacks, account }: Props) {
  const [state, formAction, pending] = useActionState(saveAccount, initialFormState);
  const value = (key: string, fallback: string) => state.values[key] ?? fallback;
  const invalid = (key: string) =>
    state.errors[key] ? { "aria-invalid": true as const, "aria-describedby": `account-${key}-error` } : {};

  return (
    <form action={formAction} className={`${kit.card} ${kit.form}`} key={state.attempt}>
      <h2 className={kit.cardTitle}>프로필 수정</h2>
      <div className={kit.grid2}>
        <div className={kit.field}>
          <label htmlFor="account-nickname" className={kit.label}>
            닉네임
          </label>
          <input
            id="account-nickname"
            name="nickname"
            maxLength={12}
            defaultValue={value("nickname", nickname)}
            className={kit.input}
            {...invalid("nickname")}
          />
          <p id="account-nickname-error" className={kit.error}>
            {state.errors.nickname}
          </p>
        </div>
        <div className={kit.field}>
          <label htmlFor="account-github" className={kit.label}>
            GitHub 주소
          </label>
          <input
            id="account-github"
            name="githubUrl"
            type="url"
            defaultValue={value("githubUrl", account.githubUrl)}
            className={kit.input}
            {...invalid("githubUrl")}
          />
          <p id="account-githubUrl-error" className={kit.error}>
            {state.errors.githubUrl}
          </p>
        </div>
      </div>
      <div className={kit.field}>
        <label htmlFor="account-bio" className={kit.label}>
          자기소개
        </label>
        <textarea
          id="account-bio"
          name="bio"
          rows={3}
          maxLength={200}
          defaultValue={value("bio", account.bio)}
          className={kit.textarea}
          style={{ minHeight: 96 }}
          {...invalid("bio")}
        />
        <p id="account-bio-error" className={kit.error}>
          {state.errors.bio}
        </p>
      </div>
      <div className={kit.field}>
        <label htmlFor="account-stacks" className={kit.label}>
          기술 스택
        </label>
        <input
          id="account-stacks"
          name="techStacks"
          defaultValue={value("techStacks", techStacks)}
          className={kit.input}
          aria-describedby="account-stacks-hint"
        />
        <p id="account-stacks-hint" className={kit.hint}>
          쉼표로 구분해요. 예: Java, Spring, Unity
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
