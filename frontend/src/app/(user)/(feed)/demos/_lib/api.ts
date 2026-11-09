import "server-only";
import { cache } from "react";
import { ApiRequestError, apiFetch, hasBackend, type ApiResponse } from "@/app/_lib/api/client";
import type {
  DemoComment,
  DemoDetail,
  DemoSort,
  DemoTemplate,
  DemoTemplateSummary,
  DemoUsage,
  DemoVersion,
  DemoVersionConfig,
} from "@/app/_lib/types/demo";
import { createMockDemoComments, mockDemoTags, mockDemos } from "./mock";

// 커서 페이지네이션 응답. 데모는 백엔드가 이미 있으므로 MOCK_EXCEPT_DM이어도 실서버를 쓴다.
type CursorResponse<T> = { content: T[]; nextCursor?: string; hasNext: boolean; size: number };

export type DemoPage = { items: DemoDetail[]; nextCursor: string | null };

export async function getServerTime(): Promise<number> {
  return Date.now();
}

export async function getDemoTags(): Promise<string[]> {
  if (!hasBackend) return mockDemoTags;
  const { data } = await apiFetch<ApiResponse<{ name: string }[]>>("/api/v1/tags");
  return data.map((tag) => tag.name);
}

export async function getDemos(
  tag: string | undefined,
  sort: DemoSort,
  cursor?: string,
): Promise<DemoPage> {
  if (!hasBackend) {
    const list = mockDemos.filter((demo) => !tag || demo.techTags.includes(tag));
    if (sort === "latest") return { items: [...list].sort((a, b) => b.createdAt.localeCompare(a.createdAt)), nextCursor: null };
    if (sort === "popular") return { items: [...list].sort((a, b) => b.likeCount - a.likeCount), nextCursor: null };
    return { items: [...list].sort((a, b) => b.playCount - a.playCount), nextCursor: null };
  }
  const query = new URLSearchParams({ sort });
  if (tag) query.set("tag", tag);
  if (cursor) query.set("cursor", cursor);
  const { data } = await apiFetch<ApiResponse<CursorResponse<DemoDetail>>>(`/api/v1/demos?${query}`);
  return { items: data.content, nextCursor: data.nextCursor ?? null };
}

/** 내 데모 수. 초안까지 포함한다. */
export async function countMyDemos(userId: number): Promise<number> {
  if (!hasBackend) return mockDemos.filter((demo) => demo.author.id === userId).length;
  const { data } = await apiFetch<ApiResponse<CursorResponse<DemoDetail>>>(
    `/api/v1/demos?userId=${userId}&sort=latest&size=50`,
  );
  return data.content.length;
}

// 레이아웃(미니홈피 틀)과 페이지가 같은 요청에서 함께 부르므로 한 번만 가져온다.
export const getDemo = cache(async function getDemo(demoId: number): Promise<DemoDetail | null> {
  if (!hasBackend) return mockDemos.find((demo) => demo.id === demoId) ?? null;
  try {
    const { data } = await apiFetch<ApiResponse<DemoDetail>>(`/api/v1/demos/${demoId}`);
    return data;
  } catch (error) {
    if (error instanceof ApiRequestError && error.status === 404) return null;
    throw error;
  }
});

type RawDemoComment = Omit<DemoComment, "content" | "replies"> & {
  content?: string | null;
  replies?: RawDemoComment[];
};

// 백엔드는 null 필드를 내려 주지 않는다. 삭제된 피드백의 content를 null로 맞춘다.
function normalizeComment(comment: RawDemoComment): DemoComment {
  return {
    ...comment,
    content: comment.content ?? null,
    replies: comment.replies?.map(normalizeComment),
  };
}

export async function getDemoComments(demoId: number): Promise<DemoComment[]> {
  if (!hasBackend) return createMockDemoComments(demoId, Date.now());
  const { data } = await apiFetch<ApiResponse<CursorResponse<RawDemoComment>>>(
    `/api/v1/demos/${demoId}/comments?size=50`,
  );
  return data.content.map(normalizeComment);
}

export async function getDemoVersions(demoId: number): Promise<DemoVersion[]> {
  if (!hasBackend) return [];
  const { data } = await apiFetch<ApiResponse<DemoVersion[]>>(`/api/v1/demos/${demoId}/versions`);
  return data;
}

export async function getDemoUsage(): Promise<DemoUsage | null> {
  if (!hasBackend) return null;
  const { data } = await apiFetch<ApiResponse<DemoUsage>>("/api/v1/demos/usage");
  return data;
}

export async function getDemoTemplates(): Promise<DemoTemplateSummary[]> {
  if (!hasBackend) return [];
  const { data } = await apiFetch<ApiResponse<DemoTemplateSummary[]>>("/api/v1/demo-templates");
  return data;
}

export async function getDemoTemplate(key: string): Promise<DemoTemplate | null> {
  if (!hasBackend) return null;
  try {
    const { data } = await apiFetch<ApiResponse<DemoTemplate>>(`/api/v1/demo-templates/${encodeURIComponent(key)}`);
    return data;
  } catch (error) {
    if (error instanceof ApiRequestError && error.status === 404) return null;
    throw error;
  }
}

export async function getVersionConfig(demoId: number, versionId: number): Promise<DemoVersionConfig | null> {
  if (!hasBackend) return null;
  try {
    const { data } = await apiFetch<ApiResponse<DemoVersionConfig>>(`/api/v1/demos/${demoId}/versions/${versionId}/config`);
    return data;
  } catch (error) {
    if (error instanceof ApiRequestError && (error.status === 404 || error.status === 403)) return null;
    throw error;
  }
}
