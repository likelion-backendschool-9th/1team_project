import type { UserSummary } from "./user";

export type PostCategory = "frontend" | "backend" | "ai" | "mobile" | "career";

export type PostSort = "latest" | "popular" | "recommended";

export type PostSummary = {
  id: number;
  author: UserSummary;
  title: string;
  excerpt: string;
  category: PostCategory;
  createdAt: string;
  // 0이면 무료 글, 양수면 잠금 해제에 드는 칩 수
  price: number;
  readMinutes: number;
  demo: { id: number; title: string; description: string } | null;
  likeCount: number;
  commentCount: number;
  repostCount: number;
  liked: boolean;
};

export type PostSeries = {
  id: number;
  author: UserSummary;
  authorTitle: string;
  title: string;
  description: string;
  tags: string[];
  status: "completed" | "ongoing";
  episodeCount: number;
  schedule: string | null;
  freeEpisodes: number;
  price: number;
  firstPostId: number;
};

export type ArticleRankEntry = {
  rank: number;
  postId: number;
  title: string;
  score: number;
};

export type ArticleRanking = Record<"views" | "likes" | "follows", ArticleRankEntry[]>;

export type SubscribedAuthor = {
  user: UserSummary;
  seriesLabel: string;
  online: boolean;
};

export type PostDetail = PostSummary & {
  content: string;
  tags: string[];
  purchased: boolean;
  seriesTitle: string | null;
  viewCount: number;
};

export type PostComment = {
  id: number;
  author: UserSummary;
  content: string;
  createdAt: string;
  likeCount: number;
};

export type BlogOwner = {
  user: UserSummary;
  bio: string;
  subscriberCount: number;
  postCount: number;
  totalViews: number;
  /** 월 구독료(원). 블로그 구독은 현금 자동이체다. */
  subscriptionPrice: number;
  subscribed: boolean;
};
