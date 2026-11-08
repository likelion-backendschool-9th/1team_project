import "server-only";
import {
  ApiRequestError,
  apiFetch,
  hasBackend,
  isMockMode,
  type ApiResponse,
  type PageResponse,
} from "@/app/_lib/api/client";
import type {
  ArticleRanking,
  BlogOwner,
  PostCategory,
  PostComment,
  PostDetail,
  PostSeries,
  PostSort,
  PostSummary,
  SubscribedAuthor,
} from "@/app/_lib/types/post";
import type { UserSummary } from "@/app/_lib/types/user";
import {
  createMockPostComments,
  createMockPostDetail,
  createMockPosts,
  createMockUserPosts,
  findMockBlogOwner,
  mockDemoOptions,
  mockArticleRanking,
  mockSeries,
  mockSubscribedAuthors,
} from "./mock";

export type PostQuery = {
  category: PostCategory | undefined;
  sort: PostSort;
  q: string | undefined;
};

export async function getServerTime(): Promise<number> {
  return Date.now();
}

// 글 CRUD는 백엔드에 있어서, 백엔드가 연결돼 있으면 MOCK_EXCEPT_DM이어도 실서버를 쓴다.
export async function getPosts({ category, sort, q }: PostQuery): Promise<PostSummary[]> {
  if (!hasBackend) {
    const posts = createMockPosts(Date.now()).filter(
      (post) =>
        (!category || post.category === category) &&
        (!q || `${post.title} ${post.excerpt}`.toLowerCase().includes(q.toLowerCase())),
    );
    return sort === "latest" ? posts : [...posts].sort((a, b) => b.likeCount - a.likeCount);
  }
  const query = new URLSearchParams({ sort });
  if (category) query.set("category", category);
  if (q) query.set("q", q);
  const { data } = await apiFetch<ApiResponse<PageResponse<PostSummary>>>(
    `/api/v1/posts?${query}`,
  );
  return data.content;
}

// 아래 조회는 아직 API가 정해지지 않아 목 데이터만 돌려준다 (프론트엔드-청사진 11장).

export async function getPopularSeries(): Promise<PostSeries[]> {
  return mockSeries;
}

export async function getArticleRanking(): Promise<ArticleRanking> {
  return mockArticleRanking;
}

export async function getSubscribedAuthors(): Promise<SubscribedAuthor[]> {
  return mockSubscribedAuthors;
}

export async function getPost(postId: number): Promise<PostDetail | null> {
  if (!hasBackend) return createMockPostDetail(postId, Date.now());
  try {
    const { data } = await apiFetch<ApiResponse<PostDetail>>(`/api/v1/posts/${postId}`);
    return data;
  } catch (error) {
    if (error instanceof ApiRequestError && error.status === 404) return null;
    throw error;
  }
}

export async function getPostComments(postId: number): Promise<PostComment[]> {
  if (isMockMode) return createMockPostComments(postId, Date.now());
  const { data } = await apiFetch<ApiResponse<{ content: PostComment[] }>>(
    `/api/v1/posts/${postId}/comments`,
  );
  return data.content;
}

export async function getUserPosts(userId: number): Promise<PostSummary[]> {
  if (!hasBackend) return createMockUserPosts(userId, Date.now());
  const { data } = await apiFetch<ApiResponse<PageResponse<PostSummary>>>(
    `/api/v1/users/${userId}/posts`,
  );
  return data.content;
}

// 블로그 주인: 회원 정보는 프로필(GET users/{userId})로 받는다.
// 소개·구독자 수·총 조회수·구독료·구독 여부는 구독 기능 전이라 비워 둔다 (블로그 구독 단계에서 채운다).
export async function getBlogOwner(userId: number): Promise<BlogOwner | null> {
  if (!hasBackend) return findMockBlogOwner(userId);
  try {
    const [{ data: user }, { data: posts }] = await Promise.all([
      apiFetch<ApiResponse<UserSummary>>(`/api/v1/users/${userId}`),
      apiFetch<ApiResponse<PageResponse<PostSummary>>>(`/api/v1/users/${userId}/posts?size=1`),
    ]);
    return {
      user: { id: user.id, nickname: user.nickname, profileImage: user.profileImage, techStacks: user.techStacks },
      bio: "",
      subscriberCount: 0,
      postCount: posts.totalElements,
      totalViews: 0,
      subscriptionPrice: 0,
      subscribed: false,
    };
  } catch (error) {
    if (error instanceof ApiRequestError && error.status === 404) return null;
    throw error;
  }
}

export async function getDemoOptions(): Promise<{ id: number; title: string }[]> {
  return mockDemoOptions;
}
