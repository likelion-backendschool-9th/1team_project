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

export type ChipPackage = { id: number; chips: number };

export type ChipHistory = {
  id: number;
  type: "charge" | "use" | "refund";
  description: string;
  amount: number;
  createdAt: string;
};

// 블로그 구독과 딥소스 플러스는 칩이 아니라 현금 월 자동이체다.
export type Subscription = {
  id: number;
  author: UserSummary;
  blogTitle: string;
  pricePerMonth: number;
  nextBillingAt: string;
  status: "active" | "canceled";
};

export type PlusMembership = {
  active: boolean;
  since: string | null;
  nextBillingAt: string | null;
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
  { id: 1, chips: 10 },
  { id: 2, chips: 50 },
  { id: 3, chips: 100 },
  { id: 4, chips: 300 },
];

export const mockHistory: ChipHistory[] = [
  { id: 1, type: "use", description: "「10만 동시접속 채팅 서버」 잠금 해제", amount: -20, createdAt: "2026-11-01T21:14:00+09:00" },
  { id: 2, type: "use", description: "정시우 미니홈피 칩 후원", amount: -30, createdAt: "2026-11-01T09:00:00+09:00" },
  { id: 3, type: "charge", description: "칩 300개 충전", amount: 300, createdAt: "2026-10-28T19:40:00+09:00" },
  { id: 4, type: "use", description: "데모 인벤토리 +1칸", amount: -10, createdAt: "2026-10-25T11:20:00+09:00" },
  { id: 5, type: "refund", description: "품질 보장제 환불 「Kafka 실습 3화」", amount: 10, createdAt: "2026-10-20T15:02:00+09:00" },
];

export const mockSubscriptions: Subscription[] = [
  { id: 1, author: mockUsers.cha, blogTitle: "차재인의 개발 블로그", pricePerMonth: 5000, nextBillingAt: "2026-12-01T09:00:00+09:00", status: "active" },
  { id: 2, author: mockUsers.yoon, blogTitle: "윤세진의 개발 블로그", pricePerMonth: 3000, nextBillingAt: "2026-12-01T09:00:00+09:00", status: "active" },
  { id: 3, author: mockUsers.seungjo, blogTitle: "김승조의 개발 블로그", pricePerMonth: 3000, nextBillingAt: "2026-11-15T09:00:00+09:00", status: "canceled" },
];

export const mockPlus: PlusMembership = {
  active: true,
  since: "2026-08-01T09:00:00+09:00",
  nextBillingAt: "2026-12-01T09:00:00+09:00",
};

export type BookmarkType = "feed" | "post" | "demo";

export type Bookmark = {
  id: number;
  type: BookmarkType;
  title: string;
  author: string;
  href: string;
  savedAt: string;
};

export const mockBookmarks: Bookmark[] = [
  { id: 1, type: "feed", title: "Unity WebGL 빌드 용량 38MB → 9MB로 줄였어요", author: "김승조", href: "/feeds/3", savedAt: "2026-11-01T20:10:00+09:00" },
  { id: 2, type: "feed", title: "Spring Batch 모니터 데모 올렸어요", author: "딥소스", href: "/feeds/6", savedAt: "2026-10-31T09:00:00+09:00" },
  { id: 3, type: "post", title: "STOMP + Spring WebSocket으로 10만 동시접속 채팅 서버 구축하기", author: "차재인", href: "/blog/posts/1", savedAt: "2026-11-02T08:30:00+09:00" },
  { id: 4, type: "post", title: "웹 워커를 이용한 프레임 드랍 없는 60fps 파티클", author: "정시우", href: "/blog/posts/302", savedAt: "2026-10-29T22:00:00+09:00" },
  { id: 5, type: "demo", title: "Three.js 태양계", author: "차재인", href: "/demos/7", savedAt: "2026-10-30T13:20:00+09:00" },
  { id: 6, type: "demo", title: "사운드 시퀀서", author: "박준하", href: "/demos/10", savedAt: "2026-10-27T18:45:00+09:00" },
];
