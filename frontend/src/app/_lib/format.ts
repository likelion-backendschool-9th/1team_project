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

const pad = (value: number) => String(value).padStart(2, "0");

export function formatDate(iso: string): string {
  const date = new Date(iso);
  return `${date.getFullYear()}.${pad(date.getMonth() + 1)}.${pad(date.getDate())}`;
}

export function formatDateTime(iso: string): string {
  const date = new Date(iso);
  return `${formatDate(iso)} ${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

export function formatWon(value: number): string {
  return `${value.toLocaleString("ko-KR")}원`;
}

/** 파일 크기. 1MB 미만은 KB, 그 이상은 MB로 보여 준다 (예: 37.2KB, 4.8MB) */
export function formatBytes(bytes: number): string {
  if (bytes <= 0) return "0KB";
  if (bytes < 1024 * 1024) return `${Math.max(bytes / 1024, 0.1).toFixed(1)}KB`;
  return `${(bytes / 1024 / 1024).toFixed(1)}MB`;
}
