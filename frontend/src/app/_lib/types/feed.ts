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
  following: boolean;
};

export type Tag = {
  id: number;
  name: string;
};
