import type { UserSummary } from "./user";

export type DmRoom = {
  user: UserSummary;
  online: boolean;
  lastMessage: string;
  lastAt: string;
  unreadCount: number;
};

export type DmMessage = {
  id: number;
  senderId: number;
  content: string;
  createdAt: string;
  read: boolean;
};
