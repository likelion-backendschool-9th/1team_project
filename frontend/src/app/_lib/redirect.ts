const DEFAULT_REDIRECT = "/main";

// 로그인 후 돌아갈 경로. 같은 사이트 안의 경로만 허용해 외부 주소로 보내는 것을 막는다.
export function parseRedirect(value: string | string[] | undefined): string {
  const raw = Array.isArray(value) ? value[0] : value;
  if (!raw || !raw.startsWith("/") || raw.startsWith("//") || raw.includes("\\")) return DEFAULT_REDIRECT;
  if (raw === "/login" || raw.startsWith("/login?") || raw.startsWith("/onboarding")) return DEFAULT_REDIRECT;
  return raw;
}
