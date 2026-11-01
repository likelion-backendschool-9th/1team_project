import { mockUsers } from "@/app/(user)/_lib/mock";
import type { UserSummary } from "@/app/_lib/types/user";

export type MyAccount = {
  bio: string;
  githubUrl: string;
  email: string;
  provider: "github" | "kakao" | "google";
  joinedAt: string;
};

export type FollowUser = UserSummary & { following: boolean; followsMe: boolean };

export type Settings = {
  notify: Record<"like" | "comment" | "follow" | "dm" | "guestbook", boolean>;
  visibility: "public" | "followers" | "private";
  allowDm: boolean;
};

export type BlockedUser = UserSummary & { blockedAt: string };

export type ChipPackage = { id: number; chips: number; price: number; bonus: number };

export type ChipHistory = {
  id: number;
  type: "charge" | "use" | "refund";
  description: string;
  amount: number;
  createdAt: string;
};

export type Subscription = {
  id: number;
  author: UserSummary;
  seriesTitle: string;
  chipsPerMonth: number;
  nextBillingAt: string;
  status: "active" | "canceled";
};

export const mockAccount: MyAccount = {
  bio: "백엔드를 좋아하는 자바 개발자. Spring으로 실시간 서비스를 만들고 Unity로 가끔 게임도 만들어요.",
  githubUrl: "https://github.com/dipsource",
  email: "dipsource@example.com",
  provider: "github",
  joinedAt: "2025-03-14T10:00:00+09:00",
};

export const mockFollowings: FollowUser[] = [
  { ...mockUsers.cha, following: true, followsMe: true },
  { ...mockUsers.kwak, following: true, followsMe: true },
  { ...mockUsers.seungjo, following: true, followsMe: false },
  { ...mockUsers.junha, following: true, followsMe: false },
  { ...mockUsers.jinyeop, following: true, followsMe: true },
];

export const mockFollowers: FollowUser[] = [
  { ...mockUsers.cha, following: true, followsMe: true },
  { ...mockUsers.kwak, following: true, followsMe: true },
  { ...mockUsers.jung, following: false, followsMe: true },
  { ...mockUsers.yoon, following: false, followsMe: true },
  { ...mockUsers.jinyeop, following: true, followsMe: true },
];

export const mockSettings: Settings = {
  notify: { like: true, comment: true, follow: true, dm: true, guestbook: false },
  visibility: "public",
  allowDm: true,
};

export const mockBlocked: BlockedUser[] = [
  { id: 31, nickname: "스팸봇123", profileImage: null, techStacks: [], blockedAt: "2026-09-12T13:00:00+09:00" },
];

export const mockPackages: ChipPackage[] = [
  { id: 1, chips: 100, price: 1100, bonus: 0 },
  { id: 2, chips: 500, price: 5500, bonus: 25 },
  { id: 3, chips: 1000, price: 11000, bonus: 80 },
  { id: 4, chips: 3000, price: 33000, bonus: 300 },
];

export const mockHistory: ChipHistory[] = [
  { id: 1, type: "use", description: "「10만 동시접속 채팅 서버」 잠금 해제", amount: -20, createdAt: "2026-11-01T21:14:00+09:00" },
  { id: 2, type: "use", description: "차재인 연재 구독 (11월)", amount: -15, createdAt: "2026-11-01T09:00:00+09:00" },
  { id: 3, type: "charge", description: "칩 1,000개 충전 (+보너스 80)", amount: 1080, createdAt: "2026-10-28T19:40:00+09:00" },
  { id: 4, type: "refund", description: "품질 보장제 환불 「Kafka 실습 3화」", amount: 10, createdAt: "2026-10-20T15:02:00+09:00" },
  { id: 5, type: "use", description: "윤세진 연재 구독 (10월)", amount: -10, createdAt: "2026-10-01T09:00:00+09:00" },
];

export const mockSubscriptions: Subscription[] = [
  { id: 1, author: mockUsers.cha, seriesTitle: "대규모 트래픽을 견디는 이벤트 드리븐 시스템", chipsPerMonth: 15, nextBillingAt: "2026-12-01T09:00:00+09:00", status: "active" },
  { id: 2, author: mockUsers.yoon, seriesTitle: "React 19 & Next.js 15 렌더링 딥다이브", chipsPerMonth: 10, nextBillingAt: "2026-12-01T09:00:00+09:00", status: "active" },
  { id: 3, author: mockUsers.seungjo, seriesTitle: "DevOps CI/CD 파이프라인", chipsPerMonth: 8, nextBillingAt: "2026-11-15T09:00:00+09:00", status: "canceled" },
];
