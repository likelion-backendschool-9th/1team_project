import type { DemoSummary } from "./demo";
import type { UserBadge, UserSummary } from "./user";

export type MiniHomeProfile = UserSummary & {
  badges: UserBadge[];
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
