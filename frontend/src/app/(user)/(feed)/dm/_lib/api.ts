import "server-only";
import { apiFetch, isMockMode, type ApiResponse, type PageResponse } from "@/app/_lib/api/client";
import type { DmMessage, DmRoom } from "@/app/_lib/types/dm";
import { createMockMessages, createMockRooms } from "./mock";

export async function getServerTime(): Promise<number> {
  return Date.now();
}

export async function getRooms(): Promise<DmRoom[]> {
  if (isMockMode) return createMockRooms(Date.now());
  const { data } = await apiFetch<ApiResponse<DmRoom[]>>("/api/v1/dm/rooms");
  return data;
}

export async function getMessages(myId: number, partnerId: number): Promise<DmMessage[]> {
  if (isMockMode) return createMockMessages(myId, partnerId, Date.now());
  const { data } = await apiFetch<ApiResponse<PageResponse<DmMessage>>>(`/api/v1/dm/${partnerId}`);
  return data.content;
}
