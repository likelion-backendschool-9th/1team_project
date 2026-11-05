import { useEffect, useRef, useState } from "react";
import type { AutocompleteSuggestion } from "@/app/_lib/types/search";
import { loadAutocomplete } from "../_actions";

/** 입력을 멈추고 이만큼 지나면 후보를 요청한다. 빠르게 치는 동안 키마다 요청하지 않는다 */
const DEBOUNCE_MS = 250;

/**
 * 입력 중인 검색어의 자동완성 후보.
 * 늦게 도착한 이전 요청의 응답(sp)이 최신 입력(spr)의 후보를 덮어쓰지 않도록, 마지막 요청의 응답만 반영한다.
 * 새 후보가 오기 전까지는 이전 후보를 그대로 보여 줘서 목록이 깜빡이지 않게 한다.
 */
export function useAutocomplete(query: string): AutocompleteSuggestion[] {
  const [suggestions, setSuggestions] = useState<AutocompleteSuggestion[]>([]);
  const latestRequest = useRef(0);
  const trimmed = query.trim();

  useEffect(() => {
    const request = ++latestRequest.current;
    if (!trimmed) return;
    const timer = setTimeout(async () => {
      const result = await loadAutocomplete(trimmed);
      if (request === latestRequest.current) setSuggestions(result);
    }, DEBOUNCE_MS);
    return () => clearTimeout(timer);
  }, [trimmed]);

  return trimmed ? suggestions : [];
}
