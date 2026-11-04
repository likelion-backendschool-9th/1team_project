import "server-only";
import { mockUsers } from "@/app/(user)/_lib/mock";
import { apiFetch, isMockMode, type ApiResponse } from "@/app/_lib/api/client";
import type { UserSummary } from "@/app/_lib/types/user";

export type SearchType = "all" | "user" | "demo" | "post";

/** 검색 결과 정렬. relevance(관련도순)가 기본값이다 */
export type SearchSort = "relevance" | "latest";

export type SearchResult = {
  users: UserSummary[];
  demos: { id: number; title: string; description: string; author: string; likeCount: number }[];
  posts: { id: number; title: string; excerpt: string; author: string; price: number }[];
};

const demos: SearchResult["demos"] = [
  { id: 1, title: "실시간 채팅앱", description: "WebSocket + STOMP 기반 실시간 채팅", author: "차재인", likeCount: 42 },
  { id: 2, title: "날씨 대시보드", description: "React 19 Suspense 날씨 대시보드", author: "곽다윗", likeCount: 31 },
  { id: 3, title: "오목 게임", description: "Unity WebGL 2인 오목", author: "김승조", likeCount: 27 },
  { id: 9, title: "Spring Batch 모니터", description: "배치 잡 실행 현황 시각화", author: "딥소스", likeCount: 33 },
  { id: 8, title: "마크다운 에디터", description: "React 실시간 미리보기 에디터", author: "곽다윗", likeCount: 24 },
];

const posts: SearchResult["posts"] = [
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

export async function search(q: string, sort: SearchSort): Promise<SearchResult> {
  if (isMockMode)
    return {
      users: latestFirst(Object.values(mockUsers).filter((user) => match(`${user.nickname} ${user.techStacks.join(" ")}`, q)), sort),
      demos: latestFirst(demos.filter((demo) => match(`${demo.title} ${demo.description}`, q)), sort),
      posts: latestFirst(posts.filter((post) => match(`${post.title} ${post.excerpt}`, q)), sort),
    };
  const query = new URLSearchParams({ q });
  if (sort !== "relevance") query.set("sort", sort);
  const { data } = await apiFetch<ApiResponse<SearchResult>>(`/api/v1/search?${query}`);
  return data;
}

export async function getPopularKeywords(): Promise<string[]> {
  if (isMockMode) return ["React", "Spring", "WebSocket", "Unity", "Next.js", "채팅", "게임"];
  const { data } = await apiFetch<ApiResponse<string[]>>("/api/v1/search/popular");
  return data;
}
