import "server-only";
import {
  ApiRequestError,
  apiFetch,
  isMockMode,
  type ApiResponse,
  type PageResponse,
} from "@/app/_lib/api/client";
import type { DemoComment, DemoDetail, DemoSort } from "@/app/_lib/types/demo";
import { createMockDemoComments, mockDemoTags, mockDemos } from "./mock";

export async function getServerTime(): Promise<number> {
  return Date.now();
}

export async function getDemoTags(): Promise<string[]> {
  return mockDemoTags;
}

export async function getDemos(tag: string | undefined, sort: DemoSort): Promise<DemoDetail[]> {
  if (isMockMode) {
    const list = mockDemos.filter((demo) => !tag || demo.techTags.includes(tag));
    if (sort === "latest") return [...list].sort((a, b) => b.createdAt.localeCompare(a.createdAt));
    if (sort === "popular") return [...list].sort((a, b) => b.likeCount - a.likeCount);
    return [...list].sort((a, b) => b.playCount - a.playCount);
  }
  const query = new URLSearchParams({ sort });
  if (tag) query.set("tag", tag);
  const { data } = await apiFetch<ApiResponse<PageResponse<DemoDetail>>>(`/api/v1/demos?${query}`);
  return data.content;
}

export async function getDemo(demoId: number): Promise<DemoDetail | null> {
  if (isMockMode) return mockDemos.find((demo) => demo.id === demoId) ?? null;
  try {
    const { data } = await apiFetch<ApiResponse<DemoDetail>>(`/api/v1/demos/${demoId}`);
    return data;
  } catch (error) {
    if (error instanceof ApiRequestError && error.status === 404) return null;
    throw error;
  }
}

export async function getDemoComments(demoId: number): Promise<DemoComment[]> {
  if (isMockMode) return createMockDemoComments(demoId, Date.now());
  const { data } = await apiFetch<ApiResponse<PageResponse<DemoComment>>>(
    `/api/v1/demos/${demoId}/comments`,
  );
  return data.content;
}
