import type { UserSummary } from "./user";

export type PostCategory = "frontend" | "backend" | "ai" | "mobile" | "career" | "game" | "uxui";

export type PostSort = "latest" | "popular" | "recommended";

export type PostSummary = {
  id: number;
  author: UserSummary;
  title: string;
  excerpt: string;
  category: PostCategory;
  createdAt: string;
  // 0이면 칩으로 살 수 없는 글(무료 글이거나 시리즈 구매자 전용 회차), 양수면 잠금 해제에 드는 칩 수
  price: number;
  readMinutes: number;
  /** 연결한 데모 (피드 카드의 데모와 같은 요약). 목업에는 썸네일·태그가 없을 수 있다 */
  demo: { id: number; title: string; description: string; thumbnail?: string | null; techTags?: string[] } | null;
  likeCount: number;
  commentCount: number;
  repostCount: number;
  liked: boolean;
  /** 조회수. 목업 데이터에는 없을 수 있다 */
  viewCount?: number;
  /** 잠긴 글인지. 시리즈 구매자 전용 회차는 price가 0이어도 true. 목업에는 없을 수 있다 */
  paid?: boolean;
  /** 들어 있는 시리즈. 없으면 null */
  seriesId?: number | null;
};

export type PostSeries = {
  id: number;
  author: UserSummary;
  authorTitle: string;
  title: string;
  description: string;
  tags: string[];
  status: "completed" | "ongoing";
  episodeCount: number;
  schedule: string | null;
  freeEpisodes: number;
  /** 시리즈 가격(원). 한 번 사면 이후 회차까지 계속 읽는다. 0이면 무료 시리즈 */
  price: number;
  /** 1화 글. 회차가 없으면 null */
  firstPostId: number | null;
  /** 산 사람 수 */
  purchaseCount?: number;
};

export type SeriesEpisode = {
  postId: number;
  /** 회차 번호(1부터) */
  order: number;
  title: string;
  /** 시리즈를 사야 읽을 수 있는 회차인지 */
  paid: boolean;
  createdAt: string;
};

export type SeriesDetail = {
  series: PostSeries;
  /** 본문을 다 읽을 수 있는지 (작가 본인·구매자) */
  purchased: boolean;
  mine: boolean;
  episodes: SeriesEpisode[];
};

export type SeriesPurchase = {
  series: PostSeries;
  /** 결제한 금액(원) */
  paidPrice: number;
  purchasedAt: string;
};

export type ArticleRankEntry = {
  rank: number;
  postId: number;
  title: string;
  score: number;
};

export type ArticleRanking = Record<"views" | "likes" | "follows", ArticleRankEntry[]>;

export type PostDetail = PostSummary & {
  content: string;
  tags: string[];
  purchased: boolean;
  seriesTitle: string | null;
  /** 시리즈 안 회차 번호. 시리즈 밖 글이면 null */
  seriesOrder?: number | null;
  viewCount: number;
  /** 내가 북마크했는지 */
  bookmarked: boolean;
};

export type PostComment = {
  id: number;
  author: UserSummary;
  content: string | null; // 삭제된 댓글은 null
  createdAt: string;
  likeCount: number;
  liked: boolean;
  replies?: PostComment[];
};

export type BlogOwner = {
  user: UserSummary;
  bio: string;
  postCount: number;
  totalViews: number;
};
