import "server-only";
import { ApiRequestError, apiFetch, isMockMode, type ApiResponse, type PageResponse } from "@/app/_lib/api/client";
import type { Feed, FeedComment } from "@/app/_lib/types/feed";
import { createMockFeedComments, createMockFeeds } from "../../../_lib/mock";

export async function getServerTime(): Promise<number> {
  return Date.now();
}

export async function getFeed(feedId: number): Promise<Feed | null> {
  if (isMockMode) return createMockFeeds(Date.now()).find((feed) => feed.id === feedId) ?? null;
  try {
    const { data } = await apiFetch<ApiResponse<Feed>>(`/api/v1/feeds/${feedId}`);
    return data;
  } catch (error) {
    if (error instanceof ApiRequestError && error.status === 404) return null;
    throw error;
  }
}

export async function getFeedComments(feedId: number): Promise<FeedComment[]> {
  if (isMockMode) return createMockFeedComments(feedId, Date.now());
  const { data } = await apiFetch<ApiResponse<PageResponse<FeedComment>>>(
    `/api/v1/feeds/${feedId}/comments`,
  );
  return data.content;
}
