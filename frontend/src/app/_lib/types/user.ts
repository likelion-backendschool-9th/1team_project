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
  /** 딥소스 플러스 구독 여부. 고급 배지는 플러스 혜택이다. */
  plus: boolean;
  /** 회사 인증을 마친 경우 프로필에 보여 줄 회사 이름 */
  company: string | null;
  followerCount: number;
  followingCount: number;
  chipBalance: number;
  onboardingIncomplete?: boolean;
  /** USER / ADMIN / MASTER(최고 관리자). 관리자 화면 접근 확인용 */
  role?: "USER" | "ADMIN" | "MASTER";
  /** 칩으로 늘린 데모 인벤토리 칸 수 */
  extraInventorySlots?: number;
};

export type Following = UserSummary & {
  badges: UserBadge[];
  online: boolean;
};

export type RankingPeriod = "daily" | "weekly" | "monthly";
