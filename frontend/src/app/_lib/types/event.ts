export type EventStatus = "ongoing" | "upcoming" | "ended";

export type DevEvent = {
  id: number;
  title: string;
  summary: string;
  content: string;
  status: EventStatus;
  startAt: string;
  endAt: string;
  reward: string;
  emoji: string;
  participantCount: number;
  ctaLabel: string;
  ctaHref: string;
};
