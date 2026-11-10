"use client";

import { useEffect } from "react";
import { updatePresence } from "../_actions";

const INTERVAL = 60_000;

// 화면을 보고 있는 동안 1분마다 접속 상태를 갱신한다. 렌더링 결과는 없다.
export function PresenceHeartbeat() {
  useEffect(() => {
    function beat() {
      if (!document.hidden) void updatePresence();
    }
    beat();
    const timer = window.setInterval(beat, INTERVAL);
    return () => window.clearInterval(timer);
  }, []);

  return null;
}
