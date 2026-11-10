export const MAX_DEMO_MB = 10;
export const MAX_TITLE = 40;
export const MAX_DESCRIPTION = 200;

export function checkDemoZip(file: File | undefined): string | null {
  if (!file) return null;
  if (!file.name.toLowerCase().endsWith(".zip")) return "zip 파일만 올릴 수 있어요.";
  if (file.size > MAX_DEMO_MB * 1024 * 1024) return `${MAX_DEMO_MB}MB 이하만 올릴 수 있어요.`;
  return null;
}

export const MAX_BACKEND_URL = 500;

/** 입력 중 안내용. 내부망 차단 등 최종 검증은 백엔드가 한다. */
export function checkBackendUrl(raw: string): string | null {
  const value = raw.trim();
  if (!value) return "주소를 입력해 주세요.";
  if (value.length > MAX_BACKEND_URL) return `${MAX_BACKEND_URL}자 이하로 입력해 주세요.`;
  if (!/^https:\/\//i.test(value)) return "https:// 로 시작하는 주소만 쓸 수 있어요.";
  try {
    const url = new URL(value);
    if (url.username || url.password || url.search || url.hash) return "계정 정보나 ?, #는 넣을 수 없어요.";
  } catch {
    return "주소 형식이 올바르지 않아요.";
  }
  return null;
}

/** @returns 필드 이름별 오류. 없으면 빈 객체 */
export function checkBackendFields(apiBaseUrl: string, healthCheckUrl: string): Record<string, string> {
  const errors: Record<string, string> = {};
  const api = checkBackendUrl(apiBaseUrl);
  if (api) errors.apiBaseUrl = api;
  if (healthCheckUrl.trim()) {
    const health = checkBackendUrl(healthCheckUrl);
    if (health) errors.healthCheckUrl = health;
  }
  return errors;
}

export function parseTags(raw: string): string[] {
  return [...new Set(raw.split(",").map((tag) => tag.trim().toLowerCase()).filter(Boolean))].slice(0, 10);
}
