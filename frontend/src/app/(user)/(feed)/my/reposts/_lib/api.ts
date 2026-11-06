import "server-only";
import {
  apiFetch,
  ApiRequestError,
  isMockMode,
  type ApiResponse,
} from "@/app/_lib/api/client";
import type { Feed } from "@/app/_lib/types/feed";

type FeedCursorResponse = {
  content: Feed[];
  nextCursor?: string | null;
  hasNext?: boolean;
  size?: number;
};

/** GET /api/v1/users/me/reposts — 내가 리포스트한 피드 목록. 비로그인이면 빈 배열. */
export async function getMyReposts(): Promise<Feed[]> {
  if (isMockMode) {
    return [];
  }

  try {
    const { data } = await apiFetch<ApiResponse<FeedCursorResponse>>(
      "/api/v1/users/me/reposts?size=50",
    );
    return (data.content ?? []).map((item) => ({
      ...item,
      demo: item.demo ?? null,
      author: {
        id: item.author.id,
        nickname: item.author.nickname,
        profileImage: item.author.profileImage ?? null,
        techStacks: item.author.techStacks ?? [],
      },
    }));
  } catch (e) {
    if (e instanceof ApiRequestError && e.status === 401) return [];
    throw e;
  }
}
