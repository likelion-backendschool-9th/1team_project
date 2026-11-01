import type { UserSummary } from "./user";

export type DemoSummary = {
  id: number;
  title: string;
  description: string;
  thumbnail: string | null;
  techTags: string[];
  author: UserSummary;
  likeCount: number;
};
