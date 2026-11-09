export const MAX_DEMO_MB = 10;
export const MAX_TITLE = 40;
export const MAX_DESCRIPTION = 200;

export function checkDemoZip(file: File | undefined): string | null {
  if (!file) return null;
  if (!file.name.toLowerCase().endsWith(".zip")) return "zip 파일만 올릴 수 있어요.";
  if (file.size > MAX_DEMO_MB * 1024 * 1024) return `${MAX_DEMO_MB}MB 이하만 올릴 수 있어요.`;
  return null;
}

export function parseTags(raw: string): string[] {
  return [...new Set(raw.split(",").map((tag) => tag.trim().toLowerCase()).filter(Boolean))].slice(0, 10);
}
