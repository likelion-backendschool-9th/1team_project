const MINUTE = 60_000;
const HOUR = 60 * MINUTE;
const DAY = 24 * HOUR;

export function formatRelativeTime(iso: string, now: number): string {
  const diff = Math.max(0, now - new Date(iso).getTime());
  if (diff < MINUTE) return "방금 전";
  if (diff < HOUR) return `${Math.floor(diff / MINUTE)}분 전`;
  if (diff < DAY) return `${Math.floor(diff / HOUR)}시간 전`;
  if (diff < 30 * DAY) return `${Math.floor(diff / DAY)}일 전`;
  return new Date(iso).toLocaleDateString("ko-KR");
}

export function formatCount(value: number): string {
  return value.toLocaleString("ko-KR");
}
