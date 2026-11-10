export const NICKNAME_MIN = 2;
export const NICKNAME_MAX = 30;
export const BIO_MAX = 200;

const NICKNAME_PATTERN = /^[가-힣a-zA-Z0-9_]+$/;

// 클라이언트(중복 확인 전 검사)와 서버(제출 검증)가 같이 쓴다.
export function validateNickname(nickname: string): string | null {
  if (!nickname) return "닉네임을 입력해 주세요.";
  if (nickname.length < NICKNAME_MIN || nickname.length > NICKNAME_MAX)
    return `${NICKNAME_MIN}~${NICKNAME_MAX}자로 입력해 주세요.`;
  if (!NICKNAME_PATTERN.test(nickname)) return "한글, 영문, 숫자, 밑줄(_)만 쓸 수 있어요.";
  return null;
}
