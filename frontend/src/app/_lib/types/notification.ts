import type { UserSummary } from "./user";

export type NotificationType = "like" | "comment" | "follow" | "guestbook" | "repost" | "subscribe" | "sponsor";

export type AppNotification = {
  id: number;
  type: NotificationType;
  actor: UserSummary;
  message: string;
  href: string;
  createdAt: string;
  read: boolean;
};
