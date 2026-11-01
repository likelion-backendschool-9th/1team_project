import "server-only";
import { mockUsers } from "@/app/(user)/_lib/mock";
import { apiFetch, isMockMode, type ApiResponse, type PageResponse } from "@/app/_lib/api/client";
import type { AppNotification } from "@/app/_lib/types/notification";

export async function getServerTime(): Promise<number> {
  return Date.now();
}

function createMockNotifications(now: number): AppNotification[] {
  const rows: [AppNotification["type"], keyof typeof mockUsers, string, string, number, boolean][] = [
    ["like", "cha", "님이 내 데모 「Spring Batch 모니터」를 좋아해요.", "/demos/9", 4, false],
    ["comment", "kwak", "님이 내 피드에 댓글을 남겼어요: “배치 실패 알림은 어떻게 받으세요?”", "/feeds/1", 18, false],
    ["follow", "jinyeop", "님이 나를 팔로우하기 시작했어요.", "/mini-home/8", 50, false],
    ["guestbook", "seungjo", "님이 미니홈피 방명록을 남겼어요.", "/mini-home/1/guestbook", 120, true],
    ["repost", "junha", "님이 내 피드를 리포스트했어요.", "/feeds/1", 300, true],
    ["subscribe", "jung", "님이 내 블로그 연재를 구독했어요.", "/blog/1", 60 * 26, true],
    ["like", "kwak", "님이 내 블로그 글 「JPA N+1 해결기」를 좋아해요.", "/blog/posts/1", 60 * 50, true],
  ];
  return rows.map(([type, user, message, href, minutesAgo, read], index) => ({
    id: index + 1,
    type,
    actor: mockUsers[user],
    message,
    href,
    createdAt: new Date(now - minutesAgo * 60_000).toISOString(),
    read,
  }));
}

export async function getNotifications(): Promise<AppNotification[]> {
  if (isMockMode) return createMockNotifications(Date.now());
  const { data } = await apiFetch<ApiResponse<PageResponse<AppNotification>>>("/api/v1/notifications");
  return data.content;
}
