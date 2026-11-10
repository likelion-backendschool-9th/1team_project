"use client";

import { useEffect, useRef } from "react";
import { recordVisit } from "../_actions";

// 다른 사람이 미니홈피에 들어올 때마다(새로고침 포함) 방문 수를 올린다 (POST mini-homes/{userId}/visits).
// 개발 모드(StrictMode)에서 effect가 두 번 실행돼도 화면 진입 한 번에 한 번만 기록한다.
export function VisitCounter({ userId }: { userId: number }) {
  const recorded = useRef<number | null>(null);

  useEffect(() => {
    if (recorded.current === userId) return;
    recorded.current = userId;
    void recordVisit(userId);
  }, [userId]);

  return null;
}
