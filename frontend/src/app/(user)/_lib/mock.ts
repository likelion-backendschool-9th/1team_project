import type { AppNotification } from "@/app/_lib/types/notification";
import type {
  Following,
  MyProfile,
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
  seoyeon: { id: 9, nickname: "뉴렉쳐", profileImage: null, techStacks: ["Vue"] },
  minjun: { id: 10, nickname: "최멋사", profileImage: null, techStacks: ["Go"] },
  jiwoo: { id: 11, nickname: "일론머스크", profileImage: null, techStacks: ["Flutter"] },
} satisfies Record<string, UserSummary>;

// 딥소스 플러스 회원. 고급 배지·스킨·BGM은 플러스 혜택이다.
// 파도타기로 홈피마다 다른 스킨이 보이도록 대부분을 플러스로 둔다. 박준하(5)는 기본 스킨 예시.
export const mockPlusUserIds = [1, 2, 3, 4, 6, 7, 8, 9, 10, 11];

// 회사 인증을 마친 회원의 회사 태그
export const mockCompanies: Record<number, string> = { 2: "코드브릿지", 6: "라이온테크" };

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
  plus: mockPlusUserIds.includes(mockUsers.me.id),
  company: mockCompanies[mockUsers.me.id] ?? null,
  followerCount: 128,
  followingCount: 64,
  chipBalance: 1250,
};

export const mockFollowings: Following[] = [
  { ...withBadges(mockUsers.cha), online: true },
  { ...withBadges(mockUsers.kwak), online: true },
  { ...withBadges(mockUsers.seungjo), online: false },
  { ...withBadges(mockUsers.junha), online: false },
  { ...withBadges(mockUsers.jinyeop), online: true },
  { ...withBadges(mockUsers.seoyeon), online: false },
  { ...withBadges(mockUsers.minjun), online: true },
  { ...withBadges(mockUsers.jiwoo), online: false },
];

export const mockUnreadCount = 3;

const MINUTE = 60_000;

export function createMockNotifications(now: number): AppNotification[] {
  const rows: [AppNotification["type"], UserSummary, string, string, number, boolean][] = [
    ["like", mockUsers.cha, "님이 내 데모 「Spring Batch 모니터」를 좋아해요.", "/demos/9", 4, false],
    ["comment", mockUsers.kwak, "님이 내 피드에 댓글을 남겼어요: “배치 실패 알림은 어떻게 받으세요?”", "/feeds/6", 18, false],
    ["follow", mockUsers.jinyeop, "님이 나를 팔로우하기 시작했어요.", "/mini-home/8", 50, false],
    ["guestbook", mockUsers.seungjo, "님이 미니홈피 방명록을 남겼어요.", "/mini-home/1/guestbook", 120, true],
    ["sponsor", mockUsers.jung, "님이 미니홈피에 칩 30개를 후원했어요.", "/my/coins", 200, true],
    ["repost", mockUsers.junha, "님이 내 피드를 리포스트했어요.", "/feeds/6", 300, true],
    ["subscribe", mockUsers.yoon, "님이 내 블로그를 구독했어요.", "/blog/1", 60 * 26, true],
  ];
  return rows.map(([type, actor, message, href, minutesAgo, read], index) => ({
    id: index + 1,
    type,
    actor,
    message,
    href,
    createdAt: new Date(now - minutesAgo * MINUTE).toISOString(),
    read,
  }));
}
