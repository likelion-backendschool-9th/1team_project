"use client";

import { useState, useTransition } from "react";
import type { ActionResult } from "../_actions";

export function useToggle(
  initialActive: boolean,
  initialCount: number,
  action: (next: boolean) => Promise<ActionResult>,
) {
  const [state, setState] = useState({ active: initialActive, count: initialCount });
  const [error, setError] = useState<string | null>(null);
  const [pending, startTransition] = useTransition();

  function toggle() {
    const previous = state;
    const next = !previous.active;
    setState({ active: next, count: previous.count + (next ? 1 : -1) });
    setError(null);
    startTransition(async () => {
      const result = await action(next);
      if (!result.ok) {
        setState(previous);
        setError(result.message ?? "요청에 실패했어요.");
      }
    });
  }

  return { ...state, error, pending, toggle };
}
