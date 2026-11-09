import "server-only";
import { apiFetch, isMockMode, type ApiResponse } from "@/app/_lib/api/client";

export type NewsItem = {
  id: number;
  title: string;
  summary: string;
  source: string;
  category: string;
  publishedAt: string;
  url: string;
};

const items: Omit<NewsItem, "id">[] = [
  { title: "React 19.3 정식 출시… 서버 액션 디버깅 도구 강화", summary: "React 팀이 19.3을 공개했다. 서버 액션 호출 추적과 hydration 오류 메시지가 개선됐다.", source: "개발자신문", category: "frontend", publishedAt: "2026-11-02T08:10:00+09:00", url: "https://example.com/news/1" },
  { title: "Spring Boot 3.5, 가상 스레드 기본 활성화 검토", summary: "스프링 팀은 다음 마이너 버전에서 가상 스레드를 기본값으로 켜는 방안을 논의 중이다.", source: "IT데일리", category: "backend", publishedAt: "2026-11-02T07:30:00+09:00", url: "https://example.com/news/2" },
  { title: "생성형 AI 코드 리뷰 도입한 팀, PR 대기 시간 38% 감소", summary: "국내 스타트업 12곳의 사례를 분석한 결과 리뷰 대기 시간이 크게 줄었다.", source: "테크리포트", category: "ai", publishedAt: "2026-11-01T18:00:00+09:00", url: "https://example.com/news/3" },
  { title: "npm 공급망 공격 재발… 인기 패키지 3종에 악성 코드", summary: "보안 업체가 postinstall 스크립트를 악용한 공격을 탐지했다. lockfile 점검이 권고된다.", source: "보안뉴스", category: "security", publishedAt: "2026-11-01T14:20:00+09:00", url: "https://example.com/news/4" },
  { title: "Next.js 16.4, Partial Prefetching 안정화", summary: "부분 프리페치가 정식 기능이 되면서 내비게이션 체감 속도가 개선됐다.", source: "개발자신문", category: "frontend", publishedAt: "2026-10-31T11:00:00+09:00", url: "https://example.com/news/5" },
  { title: "PostgreSQL 18 베타, 비동기 I/O로 읽기 성능 2배", summary: "새 I/O 서브시스템이 순차 스캔과 VACUUM 성능을 크게 끌어올렸다.", source: "DB월간", category: "backend", publishedAt: "2026-10-30T09:40:00+09:00", url: "https://example.com/news/6" },
  { title: "Unity 6.2, WebGPU 백엔드 정식 지원", summary: "브라우저 게임 개발자들이 기다리던 WebGPU 렌더러가 프리뷰 딱지를 뗐다.", source: "게임개발뉴스", category: "game", publishedAt: "2026-10-29T16:15:00+09:00", url: "https://example.com/news/7" },
  { title: "주니어 개발자 이직 시장, 포트폴리오보다 코드 리뷰 경험 본다", summary: "채용 담당자 설문에서 협업 경험과 리뷰 참여 이력이 주요 평가 요소로 꼽혔다.", source: "테크리포트", category: "etc", publishedAt: "2026-10-28T10:00:00+09:00", url: "https://example.com/news/8" },
];

export async function getNews(q: string | undefined, category: string | undefined): Promise<NewsItem[]> {
  if (isMockMode)
    return items
      .map((item, index) => ({ ...item, id: index + 1 }))
      .filter(
        (item) =>
          (!category || item.category === category) &&
          (!q || `${item.title} ${item.summary}`.toLowerCase().includes(q.toLowerCase())),
      );
  const params = new URLSearchParams();
  if (q) params.set("q", q);
  if (category) params.set("category", category);
  const query = params.toString();
  const { data } = await apiFetch<ApiResponse<NewsItem[]>>(
    `/api/v1/search/news${query ? `?${query}` : ""}`,
  );
  return data;
}
