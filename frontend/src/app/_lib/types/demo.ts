import type { UserSummary } from "./user";

export type DemoSummary = {
  id: number;
  title: string;
  description: string;
  thumbnail: string | null;
  techTags: string[];
  author: UserSummary;
  likeCount: number;
};

export type DemoStatus = "DRAFT" | "ACTIVE" | "HIDDEN";

export type DemoSort = "trending" | "latest" | "popular";

export type DemoDetail = DemoSummary & {
  playCount: number;
  commentCount: number;
  createdAt: string;
  runtime: "web" | "unity";
  sizeMb: number;
  liked: boolean;
  bookmarked: boolean;
  status?: DemoStatus;
  currentVersionId?: number | null;
  /** 실행 주소. 공개 중인 데모에만 있다. */
  runUrl?: string | null;
};

export type DemoFeedbackType = "question" | "bug" | "praise";

export type DemoComment = {
  id: number;
  author: UserSummary;
  type: DemoFeedbackType;
  content: string | null; // 삭제된 피드백은 null
  createdAt: string;
  likeCount: number;
  liked: boolean;
  replies?: DemoComment[];
};

export type DemoVersion = {
  id: number;
  number: number;
  status: "UPLOADING" | "READY" | "FAILED" | "DELETED";
  entryPath: string;
  fileCount: number;
  totalBytes: number;
  current: boolean;
  createdAt: string;
  thumbnailStatus: "PENDING" | "READY" | "FAILED" | "SKIPPED";
  thumbnailUrl?: string;
  thumbnailError?: string;
};

export type DemoPreview = { url: string; expiresAt: string };

export type DemoUsage = {
  usedBytes: number;
  limitBytes: number;
  remainingBytes: number;
  demoCount: number;
  maxDemos: number;
};

export type DemoTemplateSummary = { key: string; title: string; description: string; usage: string };

export type DemoTemplate = DemoTemplateSummary & {
  html: string;
  css: string;
  js: string;
  mockConfig: string;
  sampleData: string;
};

export type DemoVersionConfig = {
  mockConfig?: string;
  sampleData?: string;
  /** 코드 편집기로 만든 버전에만 있다. */
  editor?: { html: string; css?: string; js?: string };
};
