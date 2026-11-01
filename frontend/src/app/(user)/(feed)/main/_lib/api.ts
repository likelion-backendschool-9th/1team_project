import "server-only";
import {
  apiFetch,
  isMockMode,
  type ApiResponse,
  type PageResponse,
} from "@/app/_lib/api/client";
import type { DemoSummary } from "@/app/_lib/types/demo";
import type { Feed, Tag } from "@/app/_lib/types/feed";
import { createMockFeeds, mockHotDemos, mockTags } from "../../_lib/mock";

export async function getServerTime(): Promise<number> {
  return Date.now();
}

export async function getTags(): Promise<Tag[]> {
  if (isMockMode) return mockTags;
  const { data } = await apiFetch<ApiResponse<Tag[]>>("/api/v1/tags");
  return data;
}

export async function getHotDemos(): Promise<DemoSummary[]> {
  if (isMockMode) return mockHotDemos;
  const { data } = await apiFetch<ApiResponse<PageResponse<DemoSummary>>>(
    "/api/v1/demos?sort=trending&size=7",
  );
  return data.content;
}

export type FeedType = "recommended" | "following";

export async function getFeeds(tag: string | undefined, type: FeedType): Promise<Feed[]> {
  if (isMockMode) {
    const feeds = createMockFeeds(Date.now());
    return type === "following" ? feeds.filter((feed) => feed.following) : feeds;
  }
  const query = new URLSearchParams({ type });
  if (tag) query.set("tag", tag);
  const { data } = await apiFetch<ApiResponse<PageResponse<Feed>>>(
    `/api/v1/feeds?${query}`,
  );
  return data.content;
}
