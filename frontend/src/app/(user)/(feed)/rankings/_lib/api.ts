import "server-only";
import { mockUsers } from "@/app/(user)/_lib/mock";
import { apiFetch, isMockMode, type ApiResponse } from "@/app/_lib/api/client";
import type { RankingPeriod, UserSummary } from "@/app/_lib/types/user";

export type RankingType = "demo" | "blog" | "follower" | "home";

export type RankingRow = {
  rank: number;
  user: UserSummary;
  title: string;
  score: number;
  change: number;
};

const subjects: Record<RankingType, string[]> = {
  demo: ["실시간 채팅앱", "CSS 로딩 컬렉션", "날씨 대시보드", "Spring Batch 모니터", "오목 게임", "플랫포머 미니게임", "마크다운 에디터", "사운드 시퀀서"],
  blog: ["10만 동시접속 채팅 서버", "라자냐 코드 회고", "Unity WebGL 38MB→9MB", "React 19 use() 실전", "JPA N+1 해결기", "Kafka Outbox 패턴", "CSS Subgrid 정복", "AOT 빌드 후기"],
  follower: ["팔로워", "팔로워", "팔로워", "팔로워", "팔로워", "팔로워", "팔로워", "팔로워"],
  home: ["방문자", "방문자", "방문자", "방문자", "방문자", "방문자", "방문자", "방문자"],
};

const scale: Record<RankingPeriod, number> = { daily: 1, weekly: 7, monthly: 30 };

export async function getRankings(type: RankingType, period: RankingPeriod): Promise<RankingRow[]> {
  if (isMockMode) {
    const users = Object.values(mockUsers);
    const offset = ["demo", "blog", "follower", "home"].indexOf(type) + Object.keys(scale).indexOf(period);
    return subjects[type].map((title, index) => ({
      rank: index + 1,
      user: users[(index + offset) % users.length],
      title,
      score: Math.round((420 - index * 43) * scale[period] * (1 + offset * 0.1)),
      change: [2, 0, -1, 3, 0, -2, 1, 0][(index + offset) % 8],
    }));
  }
  const { data } = await apiFetch<ApiResponse<RankingRow[]>>(
    `/api/v1/rankings?${new URLSearchParams({ type, period })}`,
  );
  return data;
}
