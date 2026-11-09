"use client";

import { useState, useTransition } from "react";
import { toggleIlchon } from "../_actions";
import styles from "./MiniHomeHeader.module.css";

type Props = {
  userId: number;
  nickname: string;
  following: boolean;
};

// 미니홈피 주인 팔로우 버튼. (예전 "일촌 맺기" — 관계는 팔로우로 통일했다)
export function IlchonButton({ userId, nickname, following }: Props) {
  const [active, setActive] = useState(following);
  const [error, setError] = useState<string | null>(null);
  const [pending, startTransition] = useTransition();

  function handleClick() {
    const next = !active;
    setActive(next);
    setError(null);
    startTransition(async () => {
      const result = await toggleIlchon(userId, next);
      if (!result.ok) {
        setActive(!next);
        setError(result.message ?? "잠시 후 다시 시도해 주세요.");
      }
    });
  }

  return (
    <>
      <button
        type="button"
        className={styles.button}
        aria-pressed={active}
        aria-label={active ? `${nickname}님 팔로우 취소` : `${nickname}님 팔로우`}
        disabled={pending}
        onClick={handleClick}
      >
        {active ? "✓ 팔로잉" : "+ 팔로우"}
      </button>
      <span role="alert" className="sr-only">
        {error}
      </span>
    </>
  );
}
