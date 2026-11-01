import type {
  Following,
  MyProfile,
  RankingEntry,
  RankingPeriod,
  UserBadge,
  UserSummary,
} from "@/app/_lib/types/user";

export const mockUsers = {
  me: { id: 1, nickname: "딥소스", profileImage: null, techStacks: ["Java", "Spring", "Unity"] },
  cha: { id: 2, nickname: "차재인", profileImage: null, techStacks: ["Spring"] },
  kwak: { id: 3, nickname: "곽다윗", profileImage: null, techStacks: ["프론트엔드"] },
  seungjo: { id: 4, nickname: "김승조", profileImage: null, techStacks: ["Unity"] },
  junha: { id: 5, nickname: "박준하", profileImage: null, techStacks: ["JavaScript"] },
  jung: { id: 6, nickname: "정시우", profileImage: null, techStacks: ["프론트엔드"] },
  yoon: { id: 7, nickname: "윤세진", profileImage: null, techStacks: ["프론트엔드"] },
  jinyeop: { id: 8, nickname: "김진엽", profileImage: null, techStacks: ["Kotlin"] },
} satisfies Record<string, UserSummary>;

// 사용자마다 최대 3개까지 보여 준다.
export const mockBadges: Record<number, UserBadge[]> = {
  1: [
    { id: 1, icon: null, label: "JAVA☕", tone: "java" },
    { id: 2, icon: "🔥", label: "고인물", tone: "flame" },
    { id: 3, icon: "⭐", label: "STAR", tone: "gold" },
  ],
  2: [
    { id: 4, icon: "🍃", label: "Spring장인", tone: "mint" },
    { id: 2, icon: "🔥", label: "고인물", tone: "flame" },
  ],
  3: [{ id: 10, icon: "💖", label: "cute", tone: "pink" }],
  6: [
    { id: 8, icon: "✨", label: "픽셀장인", tone: "violet" },
    { id: 9, icon: "⚡", label: "60FPS", tone: "gold" },
    { id: 10, icon: "💖", label: "cute", tone: "pink" },
  ],
};

function withBadges<T extends UserSummary>(user: T) {
  return { ...user, badges: mockBadges[user.id] ?? [] };
}

export const mockMe: MyProfile = {
  ...withBadges(mockUsers.me),
  followerCount: 128,
  followingCount: 64,
  chipBalance: 1250,
};

export const mockFollowings: Following[] = [
  { ...withBadges(mockUsers.cha), online: true },
  { ...withBadges(mockUsers.kwak), online: true },
  { ...withBadges(mockUsers.seungjo), online: false },
  { ...withBadges(mockUsers.junha), online: false },
  { ...withBadges(mockUsers.jinyeop), online: false },
];

export const mockRanking: Record<RankingPeriod, RankingEntry[]> = {
  daily: [
    { rank: 1, user: mockUsers.cha, score: 128 },
    { rank: 2, user: mockUsers.kwak, score: 96 },
    { rank: 3, user: mockUsers.seungjo, score: 71 },
  ],
  weekly: [
    { rank: 1, user: mockUsers.cha, score: 1204 },
    { rank: 2, user: mockUsers.kwak, score: 980 },
    { rank: 3, user: mockUsers.seungjo, score: 742 },
  ],
  monthly: [
    { rank: 1, user: mockUsers.kwak, score: 4210 },
    { rank: 2, user: mockUsers.cha, score: 3980 },
    { rank: 3, user: mockUsers.junha, score: 2875 },
  ],
};

export const mockUnreadCount = 3;
