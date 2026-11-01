"use client";

import { useState, useTransition } from "react";
import { toggleIlchon } from "../_actions";
import styles from "./MiniHomeHeader.module.css";

type Props = {
  userId: number;
  nickname: string;
  following: boolean;
};

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
        aria-label={`${nickname}님과 일촌 맺기`}
        disabled={pending}
        onClick={handleClick}
      >
        {active ? "✓ 일촌(동료)" : "+ 일촌(동료) 맺기"}
      </button>
      <span role="alert" className="sr-only">
        {error}
      </span>
    </>
  );
}
