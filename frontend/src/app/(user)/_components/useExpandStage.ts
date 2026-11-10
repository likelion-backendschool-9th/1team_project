"use client";

import { useEffect, useRef, useState } from "react";

/** 데모 무대를 페이지 안에서 키운다. 열 때 무대로 옮기고, Esc로 닫는다. */
export function useExpandStage<T extends HTMLElement>() {
  const [expanded, setExpanded] = useState(false);
  const ref = useRef<T>(null);

  useEffect(() => {
    if (!expanded) return;
    ref.current?.scrollIntoView({ behavior: "smooth", block: "start" });
    function handleKey(event: KeyboardEvent) {
      if (event.key === "Escape" && !document.querySelector("dialog[open]")) setExpanded(false);
    }
    document.addEventListener("keydown", handleKey);
    return () => document.removeEventListener("keydown", handleKey);
  }, [expanded]);

  return { expanded, setExpanded, ref };
}
