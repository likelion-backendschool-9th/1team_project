export type UserSummary = {
  id: number;
  nickname: string;
  profileImage: string | null;
  techStacks: string[];
};

export type MyProfile = UserSummary & {
  followerCount: number;
  followingCount: number;
  chipBalance: number;
};

export type Following = UserSummary & {
  online: boolean;
};

export type RankingEntry = {
  rank: number;
  user: UserSummary;
  score: number;
};
