"use client";

import { useEffect } from "react";
import { recordVisit } from "../_actions";

// 같은 브라우저에서는 하루 한 번만 방문 수를 올린다 (POST mini-homes/{userId}/visits).
export function VisitCounter({ userId }: { userId: number }) {
  useEffect(() => {
    const key = `visited:${userId}:${new Date().toISOString().slice(0, 10)}`;
    if (localStorage.getItem(key)) return;
    localStorage.setItem(key, "1");
    void recordVisit(userId);
  }, [userId]);

  return null;
}
