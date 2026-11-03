import "server-only";
import { apiFetch, ApiRequestError, isMockMode, type ApiResponse } from "@/app/_lib/api/client";
import { mockUsers } from "@/app/(user)/_lib/mock";

export type SocialProfile = {
  nickname: string;
  githubUsername: string;
};

type UserMe = {
  nickname: string;
  githubUsername: string | null;
  bio: string | null;
  techStacks: string[];
};

/**
 * 소셜 로그인으로 받아 온 기본 정보. 온보딩 입력칸의 초기값으로 쓴다.
 * 실서버에서는 GET /users/me 응답을 쓴다.
 */
export async function getSocialProfile(): Promise<SocialProfile> {
  if (isMockMode) return { nickname: "새내기개발자", githubUsername: "new-dipper" };
  try {
    const { data } = await apiFetch<ApiResponse<UserMe>>("/api/v1/users/me");
    return {
      nickname: data.nickname ?? "",
      githubUsername: data.githubUsername ?? "",
    };
  } catch {
    return { nickname: "", githubUsername: "" };
  }
}

/** 이미 쓰는 닉네임인지 확인한다. */
export async function isNicknameTaken(nickname: string): Promise<boolean> {
  if (isMockMode)
    return Object.values(mockUsers).some((user) => user.nickname.toLowerCase() === nickname.toLowerCase());

  try {
    const { data } = await apiFetch<ApiResponse<{ available: boolean }>>(
      `/api/v1/users/nicknames/available?nickname=${encodeURIComponent(nickname)}`,
    );
    return !data.available;
  } catch (error) {
    if (error instanceof ApiRequestError) throw error;
    return false;
  }
}

/** 온보딩 마지막 단계: 프로필·태그를 한 번에 저장한다. */
export async function completeOnboardingProfile(body: {
  nickname: string;
  bio: string;
  githubUsername: string;
  techStacks: string[];
  interestTags: string[];
}): Promise<void> {
  await apiFetch<ApiResponse<UserMe>>("/api/v1/users/me/profile", {
    method: "POST",
    body: JSON.stringify({
      nickname: body.nickname,
      bio: body.bio || null,
      githubUsername: body.githubUsername || null,
      techStacks: body.techStacks,
      interestTags: body.interestTags,
    }),
  });
}
