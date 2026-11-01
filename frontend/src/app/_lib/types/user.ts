export type UserSummary = {
  id: number;
  nickname: string;
  profileImage: string | null;
  techStacks: string[];
};

export type BadgeTone = "java" | "flame" | "gold" | "pink" | "mint" | "sky" | "violet";

export type UserBadge = {
  id: number;
  icon: string | null;
  label: string;
  tone: BadgeTone;
};

export type MyProfile = UserSummary & {
  badges: UserBadge[];
  followerCount: number;
  followingCount: number;
  chipBalance: number;
};

export type Following = UserSummary & {
  badges: UserBadge[];
  online: boolean;
};

export type RankingPeriod = "daily" | "weekly" | "monthly";

export type RankingEntry = {
  rank: number;
  user: UserSummary;
  score: number;
};
