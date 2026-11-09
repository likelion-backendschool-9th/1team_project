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
import { getMe } from "@/app/(user)/_lib/api";
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

// 연재 시리즈는 백엔드에 아직 없다. 백엔드가 연결되면 목업을 섞지 않고 비워 둔다(섹션이 숨겨진다).
export async function getPopularSeries(): Promise<PostSeries[]> {
  if (hasBackend) return [];
  return mockSeries;
}

// 랭킹 API(GET rankings)는 아직 빈 목록을 돌려주는 스텁이다.
// 그래서 좋아요 순위만 인기순 글 목록(GET posts?sort=popular)으로 채우고, 조회·팔로우 순위는 비워 둔다(탭이 숨겨진다).
export async function getArticleRanking(): Promise<ArticleRanking> {
  if (!hasBackend) return mockArticleRanking;
  const { data } = await apiFetch<ApiResponse<PageResponse<PostSummary>>>("/api/v1/posts?sort=popular&size=5");
  return {
    views: [],
    likes: data.content.map((post, index) => ({ rank: index + 1, postId: post.id, title: post.title, score: post.likeCount })),
    follows: [],
  };
}

type MySubscription = {
  id: number;
  author: UserSummary;
  blogTitle: string;
  nextBillingAt: string;
  status: "active" | "canceled";
};

// 구독 중인 작가는 내 구독 목록(GET subscriptions/me)으로 채운다. 접속 상태 API는 없어 점은 표시하지 않는다.
export async function getSubscribedAuthors(): Promise<SubscribedAuthor[]> {
  if (!hasBackend) return mockSubscribedAuthors;
  try {
    const { data } = await apiFetch<ApiResponse<MySubscription[]>>("/api/v1/subscriptions/me");
    return data.map((subscription) => ({
      user: subscription.author,
      seriesLabel: subscription.status === "canceled" ? `${subscription.blogTitle} · 해지 예정` : subscription.blogTitle,
    }));
  } catch (error) {
    if (error instanceof ApiRequestError && (error.status === 401 || error.status === 403)) return [];
    throw error;
  }
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

export type PostCommentPage = {
  comments: PostComment[];
  hasNext: boolean;
  nextCursor: string | null;
};

export async function getPostComments(postId: number): Promise<PostCommentPage> {
  if (isMockMode) return { comments: createMockPostComments(postId, Date.now()), hasNext: false, nextCursor: null };
  const { data } = await apiFetch<ApiResponse<{ content: PostComment[]; hasNext: boolean; nextCursor: string | null }>>(
    `/api/v1/posts/${postId}/comments`,
  );
  return { comments: data.content, hasNext: data.hasNext, nextCursor: data.nextCursor };
}

export async function getUserPosts(userId: number): Promise<PostSummary[]> {
  if (!hasBackend) return createMockUserPosts(userId, Date.now());
  const { data } = await apiFetch<ApiResponse<PageResponse<PostSummary>>>(
    `/api/v1/users/${userId}/posts`,
  );
  return data.content;
}

// 블로그 주인: 회원 정보는 프로필(GET users/{userId}), 구독료·구독 여부·구독자 수는 GET users/{userId}/subscription으로 받는다.
// 소개·총 조회수는 아직 API가 없어 비워 둔다.
export async function getBlogOwner(userId: number): Promise<BlogOwner | null> {
  if (!hasBackend) return findMockBlogOwner(userId);
  try {
    const [{ data: user }, { data: posts }, { data: subscription }] = await Promise.all([
      apiFetch<ApiResponse<UserSummary>>(`/api/v1/users/${userId}`),
      apiFetch<ApiResponse<PageResponse<PostSummary>>>(`/api/v1/users/${userId}/posts?size=1`),
      apiFetch<ApiResponse<{ price: number; subscribed: boolean; subscriberCount: number }>>(
        `/api/v1/users/${userId}/subscription`,
      ),
    ]);
    return {
      user: { id: user.id, nickname: user.nickname, profileImage: user.profileImage, techStacks: user.techStacks },
      bio: "",
      subscriberCount: subscription.subscriberCount,
      postCount: posts.totalElements,
      totalViews: 0,
      subscriptionPrice: subscription.price,
      subscribed: subscription.subscribed,
    };
  } catch (error) {
    if (error instanceof ApiRequestError && error.status === 404) return null;
    throw error;
  }
}

// 글에 연결할 데모 후보는 내 데모 목록(GET demos?userId=)에서 고른다.
export async function getDemoOptions(): Promise<{ id: number; title: string }[]> {
  if (!hasBackend) return mockDemoOptions;
  const me = await getMe();
  if (!me) return [];
  const { data } = await apiFetch<ApiResponse<{ content: { id: number; title: string }[] }>>(
    `/api/v1/demos?userId=${me.id}&sort=latest&size=50`,
  );
  return data.content.map(({ id, title }) => ({ id, title }));
}
