import "server-only";
import { mockUsers } from "@/app/(user)/_lib/mock";
import { apiFetch, ApiRequestError, isMockMode, type ApiResponse, type PageResponse } from "@/app/_lib/api/client";

export type SearchType = "all" | "post" | "demo" | "user" | "feed";

/** 검색 결과 정렬. relevance(관련도순)가 기본값이다 */
export type SearchSort = "relevance" | "latest";

export type SearchUser = { id: number; nickname: string; bio?: string; techStacks: string[] };
export type SearchDemo = { id: number; title: string; description?: string; author?: string; likeCount: number };
export type SearchPost = { id: number; title: string; excerpt?: string; author?: string; price: number };
export type SearchFeed = { id: number; content: string; author?: string; likeCount: number };

/** 대상 하나의 결과. total은 이 대상의 전체 결과 수다 */
type Section<T> = { items: T[]; total: number };

export type SearchResult = {
  users: Section<SearchUser>;
  demos: Section<SearchDemo>;
  posts: Section<SearchPost>;
  feeds: Section<SearchFeed>;
  /** 원래 검색어 결과는 그대로 두고 제안하는 검색어 */
  suggestedQuery?: string;
  /** KEYBOARD: 한/영 자판 보정, TYPO: 오타 교정 */
  suggestionType?: "KEYBOARD" | "TYPO";
  /** 검색 엔진 장애로 PostgreSQL 검색을 쓸 때만 온다. 고친 검색어로 찾은 결과다 */
  correctedQuery?: string;
  /** 모든 단어가 일치하는 결과가 없어서 일부 단어만 일치하는 결과를 보여 주는지 */
  relaxedMatch: boolean;
  /** 개별 탭의 전체 페이지 수. 전체 탭은 1이다 */
  totalPages: number;
};

export type SearchOutcome = { result: SearchResult } | { error: string };

type SearchAllResponse = {
  posts: { content: SearchPost[]; totalElements: number };
  feeds: { content: SearchFeed[]; totalElements: number };
  demos: { content: SearchDemo[]; totalElements: number };
  users: { content: SearchUser[]; totalElements: number };
  correctedQuery?: string;
  suggestedQuery?: string;
  suggestionType?: SearchResult["suggestionType"];
};

type SearchPageResponse = PageResponse<unknown> & {
  correctedQuery?: string;
  suggestedQuery?: string;
  suggestionType?: SearchResult["suggestionType"];
  relaxedMatch?: boolean;
};

const EMPTY = { items: [], total: 0 };

const demos: SearchDemo[] = [
  { id: 1, title: "실시간 채팅앱", description: "WebSocket + STOMP 기반 실시간 채팅", author: "차재인", likeCount: 42 },
  { id: 2, title: "날씨 대시보드", description: "React 19 Suspense 날씨 대시보드", author: "곽다윗", likeCount: 31 },
  { id: 3, title: "오목 게임", description: "Unity WebGL 2인 오목", author: "김승조", likeCount: 27 },
  { id: 9, title: "Spring Batch 모니터", description: "배치 잡 실행 현황 시각화", author: "딥소스", likeCount: 33 },
  { id: 8, title: "마크다운 에디터", description: "React 실시간 미리보기 에디터", author: "곽다윗", likeCount: 24 },
];

const posts: SearchPost[] = [
  { id: 1, title: "STOMP + Spring WebSocket으로 10만 동시접속 채팅 서버 구축하기", excerpt: "Redis Pub/Sub과 Netty 기반 소켓 분리 리팩토링 일지", author: "차재인", price: 20 },
  { id: 2, title: "오늘 리팩토링하다가 라자냐 코드 발견함", excerpt: "레이어 7겹을 2개로 압축한 후기", author: "곽다윗", price: 0 },
  { id: 3, title: "React 19 use()로 로딩 분기 절반 줄이기", excerpt: "Suspense와 서버 컴포넌트로 데이터 패칭 단순화", author: "곽다윗", price: 0 },
  { id: 4, title: "Unity WebGL 빌드 용량 38MB → 9MB", excerpt: "Brotli, ASTC, 패키지 정리 체크리스트", author: "김승조", price: 10 },
];

function match(text: string, q: string) {
  return text.toLowerCase().includes(q.toLowerCase());
}

/** 목 데이터는 작성 시각이 없어서 id가 클수록 최신이라고 본다 */
function latestFirst<T extends { id: number }>(items: T[], sort: SearchSort) {
  return sort === "latest" ? [...items].sort((a, b) => b.id - a.id) : items;
}

function section<T>(items: T[]): Section<T> {
  return { items, total: items.length };
}

function mockSearch(q: string, sort: SearchSort): SearchResult {
  return {
    users: section(latestFirst(Object.values(mockUsers).filter((user) => match(`${user.nickname} ${user.techStacks.join(" ")}`, q)), sort)),
    demos: section(latestFirst(demos.filter((demo) => match(`${demo.title} ${demo.description}`, q)), sort)),
    posts: section(latestFirst(posts.filter((post) => match(`${post.title} ${post.excerpt}`, q)), sort)),
    feeds: EMPTY,
    relaxedMatch: false,
    totalPages: 1,
  };
}

function fromAll(data: SearchAllResponse): SearchResult {
  return {
    users: { items: data.users.content, total: data.users.totalElements },
    demos: { items: data.demos.content, total: data.demos.totalElements },
    posts: { items: data.posts.content, total: data.posts.totalElements },
    feeds: { items: data.feeds.content, total: data.feeds.totalElements },
    suggestedQuery: data.suggestedQuery,
    suggestionType: data.suggestionType,
    correctedQuery: data.correctedQuery,
    relaxedMatch: false,
    totalPages: 1,
  };
}

function fromPage(type: Exclude<SearchType, "all">, data: SearchPageResponse): SearchResult {
  const hit = { items: data.content, total: data.totalElements };
  return {
    users: type === "user" ? (hit as Section<SearchUser>) : EMPTY,
    demos: type === "demo" ? (hit as Section<SearchDemo>) : EMPTY,
    posts: type === "post" ? (hit as Section<SearchPost>) : EMPTY,
    feeds: type === "feed" ? (hit as Section<SearchFeed>) : EMPTY,
    suggestedQuery: data.suggestedQuery,
    suggestionType: data.suggestionType,
    correctedQuery: data.correctedQuery,
    relaxedMatch: data.relaxedMatch === true,
    totalPages: data.totalPages,
  };
}

/** @param page 1부터 센다 */
export async function search(q: string, type: SearchType, sort: SearchSort, page: number): Promise<SearchOutcome> {
  if (isMockMode) return { result: mockSearch(q, sort) };
  const query = new URLSearchParams({ q });
  if (type !== "all") {
    query.set("type", type);
    query.set("page", String(page - 1));
  }
  if (sort !== "relevance") query.set("sort", sort);
  try {
    if (type === "all") {
      const { data } = await apiFetch<ApiResponse<SearchAllResponse>>(`/api/v1/search?${query}`);
      return { result: fromAll(data) };
    }
    const { data } = await apiFetch<ApiResponse<SearchPageResponse>>(`/api/v1/search?${query}`);
    return { result: fromPage(type, data) };
  } catch (error) {
    // 짧은 검색어 등 입력 오류는 화면에 안내하고, 그 외는 오류 화면으로 넘긴다
    if (error instanceof ApiRequestError && error.status === 400) return { error: error.error.message };
    throw error;
  }
}

export async function getPopularKeywords(): Promise<string[]> {
  if (isMockMode) return ["React", "Spring", "WebSocket", "Unity", "Next.js", "채팅", "게임"];
  const { data } = await apiFetch<ApiResponse<string[]>>("/api/v1/search/popular");
  return data;
}
