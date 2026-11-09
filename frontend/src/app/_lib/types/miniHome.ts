import type { DemoSummary } from "./demo";
import type { UserBadge, UserSummary } from "./user";

export type MiniHomeSkin =
  | "cream"
  | "midnight"
  | "mint"
  | "cherry"
  | "sky"
  | "lavender"
  | "peach"
  | "forest"
  | "sunset";

export type MiniHomeProfile = UserSummary & {
  badges: UserBadge[];
  plus: boolean;
  company: string | null;
  /** 스킨·BGM은 딥소스 플러스 전용 */
  skin: MiniHomeSkin;
  /** 미니홈피에 보여 줄 데모 id (일반 3개, 플러스 6개까지) */
  showcaseDemoIds: number[];
  /** 칩으로 늘린 데모 인벤토리 칸 수 */
  extraInventorySlots: number;
  headline: string;
  domain: string | null;
  bgm: string | null;
  quote: string | null;
  stackTags: string[];
  featuredDemoId: number | null;
  featuredPostId: number | null;
  following: boolean;
};

export type VisitorStats = {
  today: number;
  total: number;
};

export type GithubLanguage = {
  name: string;
  percent: number;
};

export type GithubContributions = {
  totalCommits: number;
  // 오래된 날부터 최근 순. level은 0~4 단계
  days: { date: string; level: number }[];
};

export type GithubRepo = {
  name: string;
  stars: number;
};

export type MiniHomeDemo = DemoSummary & {
  engine: string;
  /** 실행 주소. 공개 중이고 올린 파일이 있을 때만 온다 */
  runUrl?: string | null;
  playCount: number;
  commentCount: number;
};

export type MiniHomePost = {
  id: number;
  title: string;
  membership: boolean;
};

export type MiniHomeComment = {
  id: number;
  author: UserSummary;
  content: string;
  createdAt: string;
  secret: boolean;
};
