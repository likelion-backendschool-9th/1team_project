import type { EventStatus } from "@/app/_lib/types/event";

export const statusLabels: Record<EventStatus, string> = {
  ongoing: "진행 중",
  upcoming: "예정",
  ended: "종료",
};
