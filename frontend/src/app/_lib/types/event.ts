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
  /** 참여하기 버튼이 보내는 사이트 안 경로 */
  ctaHref: string;
  /** 대표 이미지. 없으면 null이고 이모지 배너를 보여 준다 */
  imageUrl: string | null;
};
