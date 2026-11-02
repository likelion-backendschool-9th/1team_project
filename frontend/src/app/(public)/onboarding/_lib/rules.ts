export const NICKNAME_MIN = 2;
export const NICKNAME_MAX = 30;
export const BIO_MAX = 200;
export const MAX_STACKS = 10;
export const STACK_MAX_LENGTH = 20;

export const suggestedStacks = [
  "Java",
  "Spring",
  "JavaScript",
  "TypeScript",
  "React",
  "Next.js",
  "Vue",
  "Node.js",
  "Python",
  "Kotlin",
  "Unity",
  "Flutter",
];

const NICKNAME_PATTERN = /^[가-힣a-zA-Z0-9_]+$/;
const GITHUB_PATTERN = /^[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,38})$/;

// 클라이언트(중복 확인 전 검사)와 서버(제출 검증)가 같이 쓴다.
export function validateNickname(nickname: string): string | null {
  if (!nickname) return "닉네임을 입력해 주세요.";
  if (nickname.length < NICKNAME_MIN || nickname.length > NICKNAME_MAX)
    return `${NICKNAME_MIN}~${NICKNAME_MAX}자로 입력해 주세요.`;
  if (!NICKNAME_PATTERN.test(nickname)) return "한글, 영문, 숫자, 밑줄(_)만 쓸 수 있어요.";
  return null;
}

export function validateGithubUsername(username: string): string | null {
  if (username && !GITHUB_PATTERN.test(username)) return "올바른 GitHub 사용자명이 아니에요.";
  return null;
}

// 쉼표로 이어 붙인 기술 스택을 정리한다. 빈 값과 중복(대소문자 무시)은 뺀다.
export function parseStacks(value: string): string[] {
  const seen = new Set<string>();
  return value
    .split(",")
    .map((stack) => stack.trim())
    .filter((stack) => {
      const key = stack.toLowerCase();
      if (!stack || seen.has(key)) return false;
      seen.add(key);
      return true;
    });
}
