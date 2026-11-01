import type {
  Following,
  MyProfile,
  RankingEntry,
  UserSummary,
} from "@/app/_lib/types/user";

export const mockUsers = {
  me: { id: 1, nickname: "김진열", profileImage: null, techStacks: ["Java", "Spring", "Unity"] },
  kim: { id: 2, nickname: "김개발", profileImage: null, techStacks: ["Spring"] },
  park: { id: 3, nickname: "박개발", profileImage: null, techStacks: ["프론트엔드"] },
  lee: { id: 4, nickname: "이개발", profileImage: null, techStacks: ["Unity"] },
  choi: { id: 5, nickname: "최개발", profileImage: null, techStacks: ["JavaScript"] },
} satisfies Record<string, UserSummary>;

export const mockMe: MyProfile = {
  ...mockUsers.me,
  followerCount: 128,
  followingCount: 64,
  chipBalance: 1250,
};

export const mockFollowings: Following[] = [
  { ...mockUsers.kim, online: true },
  { ...mockUsers.park, online: true },
  { ...mockUsers.lee, online: false },
  { ...mockUsers.choi, online: false },
];

export const mockRanking: RankingEntry[] = [
  { rank: 1, user: mockUsers.kim, score: 1204 },
  { rank: 2, user: mockUsers.park, score: 980 },
  { rank: 3, user: mockUsers.lee, score: 742 },
];

export const mockUnreadCount = 3;
