import "server-only";
import { apiFetch, isMockMode, type ApiResponse } from "@/app/_lib/api/client";
import type { AutocompleteSuggestion } from "@/app/_lib/types/search";

const MAX_SUGGESTIONS = 8;

// 백엔드 연결 전 확인용. 실제 사전은 백엔드가 태그·공개 제목에서 만든다.
const mockTags = ["Spring Boot", "Spring Security", "React", "Next.js", "TypeScript", "WebSocket", "Unity", "스프링 부트"];
const mockWords = ["spring", "stomp", "redis", "react", "refactoring", "websocket", "unity", "webgl", "스프링", "리팩토링", "채팅"];

function isSuggestion(value: unknown): value is AutocompleteSuggestion {
  if (!value || typeof value !== "object") return false;
  const { text, type } = value as Record<string, unknown>;
  return typeof text === "string" && (type === "TAG" || type === "WORD" || type === "TERM" || type === "CORRECTION");
}

/** 입력 중인 검색어의 자동완성 후보. 목 데이터는 한글 조합 중 처리 없이 접두어만 비교한다 */
export async function getAutocomplete(q: string): Promise<AutocompleteSuggestion[]> {
  if (isMockMode) {
    const query = q.toLowerCase();
    const lastSpace = query.lastIndexOf(" ");
    const leading = query.slice(0, lastSpace + 1);
    const lastWord = query.slice(lastSpace + 1);
    return [
      ...mockTags.filter((tag) => tag.toLowerCase().startsWith(query)).map((text) => ({ text, type: "TAG" as const })),
      ...mockWords.filter((word) => word.startsWith(lastWord)).map((word) => ({ text: leading + word, type: "WORD" as const })),
    ].slice(0, MAX_SUGGESTIONS);
  }
  const { data } = await apiFetch<ApiResponse<unknown>>(`/api/v1/search/autocomplete?${new URLSearchParams({ q })}`);
  return Array.isArray(data) ? data.filter(isSuggestion).slice(0, MAX_SUGGESTIONS) : [];
}
