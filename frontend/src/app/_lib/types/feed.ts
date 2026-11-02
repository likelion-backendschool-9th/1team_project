import type { DemoSummary } from "./demo";
import type { UserSummary } from "./user";

export type Feed = {
  id: number;
  author: UserSummary;
  content: string;
  createdAt: string;
  demo: DemoSummary | null;
  likeCount: number;
  commentCount: number;
  repostCount: number;
  liked: boolean;
  reposted: boolean;
  bookmarked: boolean;
  following: boolean;
};

export type Tag = {
  id: number;
  name: string;
};

export type FeedComment = {
  id: number;
  author: UserSummary;
  content: string;
  createdAt: string;
  likeCount: number;
  liked: boolean;
  replies?: FeedComment[];
};
