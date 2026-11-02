import "server-only";
import { isMockMode } from "@/app/_lib/api/client";
import { mockUsers } from "@/app/(user)/_lib/mock";

export type SocialProfile = {
  nickname: string;
  githubUsername: string;
};

// 소셜 로그인으로 받아 온 기본 정보. 온보딩 입력칸의 초기값으로 쓴다.
// TODO(백엔드 연동): 기획서에 명세가 생기면 실제 API로 교체한다.
export async function getSocialProfile(): Promise<SocialProfile> {
  if (isMockMode) return { nickname: "새내기개발자", githubUsername: "new-dipper" };
  return { nickname: "", githubUsername: "" };
}

// 이미 쓰는 닉네임인지 확인한다. 목 모드에서는 목 회원의 닉네임을 사용 중으로 본다.
export async function isNicknameTaken(nickname: string): Promise<boolean> {
  if (isMockMode)
    return Object.values(mockUsers).some((user) => user.nickname.toLowerCase() === nickname.toLowerCase());
  return false;
}
