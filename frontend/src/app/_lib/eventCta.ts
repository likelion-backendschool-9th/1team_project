// 이벤트 참여하기 버튼이 보낼 경로. 관리자 폼의 선택지와 서버 액션 검증이 같이 쓴다.
export const EVENT_CTA_PRESETS = [
  { href: "/demos/upload", label: "데모 올리기" },
  { href: "/demos/studio", label: "코드로 데모 만들기" },
  { href: "/blog/write", label: "블로그 글쓰기 (시리즈 연재·인증)" },
  { href: "/main", label: "피드에 글쓰기" },
  { href: "/mini-home/me", label: "내 미니홈피 꾸미기" },
  { href: "/rankings", label: "랭킹 보기" },
  { href: "/demos", label: "데모 둘러보기" },
] as const;

/** 프리셋에 없는 경로를 직접 입력할 때 select 값 */
export const CUSTOM_CTA = "custom";

/** /로 시작하는 사이트 안 경로인지. //나 공백이 있으면 다른 사이트로 갈 수 있어 막는다 (백엔드와 같은 규칙) */
export function isInternalPath(href: string) {
  return /^\/(?![/\\])[^\s\\]*$/.test(href);
}
