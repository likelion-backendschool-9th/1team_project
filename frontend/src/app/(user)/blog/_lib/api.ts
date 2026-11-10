import "server-only";
import {
  ApiRequestError,
  apiFetch,
  hasBackend,
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
  SeriesDetail,
  SeriesPurchase,
} from "@/app/_lib/types/post";
import type { DemoSummary } from "@/app/_lib/types/demo";
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
  findMockSeries,
  mockSeries,
  mockSeriesPurchases,
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

// 시리즈 API는 백엔드에 있어서 MOCK_EXCEPT_DM이어도 실서버를 쓴다.
/** 인기 시리즈 (산 사람이 많은 순) */
export async function getPopularSeries(): Promise<PostSeries[]> {
  if (!hasBackend) return mockSeries;
  const { data } = await apiFetch<ApiResponse<PostSeries[]>>("/api/v1/series?size=6");
  return data;
}

/** 작가의 시리즈 목록. 최근에 만든 순 */
export async function getUserSeries(userId: number): Promise<PostSeries[]> {
  if (!hasBackend) return mockSeries.filter((item) => item.author.id === userId);
  const { data } = await apiFetch<ApiResponse<PostSeries[]>>(`/api/v1/users/${userId}/series`);
  return data;
}

/** 시리즈 화면. 없거나 차단 관계면 null */
export async function getSeries(seriesId: number): Promise<SeriesDetail | null> {
  if (!hasBackend) return findMockSeries(seriesId);
  try {
    const { data } = await apiFetch<ApiResponse<SeriesDetail>>(`/api/v1/series/${seriesId}`);
    return data;
  } catch (error) {
    if (error instanceof ApiRequestError && error.status === 404) return null;
    throw error;
  }
}

/** 내가 산 시리즈. 비로그인이면 빈 목록 */
export async function getSeriesPurchases(): Promise<SeriesPurchase[]> {
  if (!hasBackend) return mockSeriesPurchases;
  try {
    const { data } = await apiFetch<ApiResponse<SeriesPurchase[]>>("/api/v1/users/me/series-purchases");
    return data;
  } catch (error) {
    if (error instanceof ApiRequestError && (error.status === 401 || error.status === 403)) return [];
    throw error;
  }
}

/** 글쓰기에서 고를 내 시리즈 */
export async function getMySeries(): Promise<PostSeries[]> {
  const me = await getMe();
  return me ? getUserSeries(me.id) : [];
}

// 랭킹 API(GET rankings)는 아직 빈 목록을 돌려주는 스텁이다.
// 그래서 조회 순위는 조회수순(GET posts?sort=views), 좋아요 순위는 인기순(GET posts?sort=popular) 글 목록으로 채운다.
// 팔로우 순위는 무엇을 셀지 정해지지 않아 비워 둔다(탭이 숨겨진다).
export async function getArticleRanking(): Promise<ArticleRanking> {
  if (!hasBackend) return mockArticleRanking;
  const [{ data: byViews }, { data: byLikes }] = await Promise.all([
    apiFetch<ApiResponse<PageResponse<PostSummary>>>("/api/v1/posts?sort=views&size=5"),
    apiFetch<ApiResponse<PageResponse<PostSummary>>>("/api/v1/posts?sort=popular&size=5"),
  ]);
  const rank = (posts: PostSummary[], score: (post: PostSummary) => number) =>
    posts.map((post, index) => ({ rank: index + 1, postId: post.id, title: post.title, score: score(post) }));
  return {
    views: rank(byViews.content, (post) => post.viewCount ?? 0),
    likes: rank(byLikes.content, (post) => post.likeCount),
    follows: [],
  };
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

// 댓글 API는 백엔드에 있어서 MOCK_EXCEPT_DM이어도 실서버를 쓴다.
export async function getPostComments(postId: number): Promise<PostCommentPage> {
  if (!hasBackend) return { comments: createMockPostComments(postId, Date.now()), hasNext: false, nextCursor: null };
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

// 블로그 주인: 회원 정보는 프로필(GET users/{userId}), 소개·글 수·총 조회수는 GET users/{userId}/blog로 받는다.
export async function getBlogOwner(userId: number): Promise<BlogOwner | null> {
  if (!hasBackend) return findMockBlogOwner(userId);
  try {
    const [{ data: user }, { data: stats }] = await Promise.all([
      apiFetch<ApiResponse<UserSummary>>(`/api/v1/users/${userId}`),
      apiFetch<ApiResponse<{ bio: string; postCount: number; totalViews: number }>>(`/api/v1/users/${userId}/blog`),
    ]);
    return {
      user: { id: user.id, nickname: user.nickname, profileImage: user.profileImage, techStacks: user.techStacks },
      bio: stats.bio,
      postCount: stats.postCount,
      totalViews: stats.totalViews,
    };
  } catch (error) {
    if (error instanceof ApiRequestError && error.status === 404) return null;
    throw error;
  }
}

// 글에 연결할 데모 후보는 피드 작성창처럼 내 데모 목록(GET demos?userId=)에서 고른다.
export async function getDemoOptions(): Promise<DemoSummary[]> {
  if (!hasBackend) return mockDemoOptions;
  const me = await getMe();
  if (!me) return [];
  const { data } = await apiFetch<ApiResponse<PageResponse<DemoSummary>>>(
    `/api/v1/demos?userId=${me.id}&sort=latest&size=50`,
  );
  return data.content;
}
