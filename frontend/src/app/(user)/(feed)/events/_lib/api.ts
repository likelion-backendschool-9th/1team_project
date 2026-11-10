import "server-only";
import { ApiRequestError, apiFetch, isMockMode, type ApiResponse, type PageResponse } from "@/app/_lib/api/client";
import { mockEvents } from "@/app/_lib/mockEvents";
import type { DevEvent, EventStatus } from "@/app/_lib/types/event";

export async function getEvents(status: EventStatus | undefined): Promise<DevEvent[]> {
  if (isMockMode) return mockEvents.filter((event) => !status || event.status === status);
  const query = status ? `?${new URLSearchParams({ status })}` : "";
  const { data } = await apiFetch<ApiResponse<PageResponse<DevEvent>>>(`/api/v1/events${query}`);
  return data.content;
}

export async function getEvent(eventId: number): Promise<DevEvent | null> {
  if (isMockMode) return mockEvents.find((event) => event.id === eventId) ?? null;
  try {
    const { data } = await apiFetch<ApiResponse<DevEvent>>(`/api/v1/events/${eventId}`);
    return data;
  } catch (error) {
    if (error instanceof ApiRequestError && error.status === 404) return null;
    throw error;
  }
}
