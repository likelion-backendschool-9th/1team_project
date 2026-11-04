import "server-only";
import { redirect } from "next/navigation";
import {
  apiFetch,
  ApiRequestError,
  hasBackend,
  type ApiResponse,
  type PageResponse,
} from "@/app/_lib/api/client";
import type { DmMessage, DmRoom } from "@/app/_lib/types/dm";
import { createMockMessages, createMockRooms } from "./dmMock";

// DM은 로그인한 회원만 쓴다. 로그인이 풀렸으면(401) 로그인 화면으로 보낸다.
async function fetchOrLogin<T>(path: `/api/v1/${string}`): Promise<T> {
  try {
    return await apiFetch<T>(path);
  } catch (error) {
    if (error instanceof ApiRequestError && error.status === 401)
      redirect("/login");
    throw error;
  }
}

export async function getRooms(): Promise<DmRoom[]> {
  if (!hasBackend) return createMockRooms(Date.now());
  const { data } = await fetchOrLogin<ApiResponse<DmRoom[]>>("/api/v1/dm/rooms");
  return data;
}

export async function getMessages(myId: number, partnerId: number): Promise<DmMessage[]> {
  if (!hasBackend) return createMockMessages(myId, partnerId, Date.now());
  const { data } = await fetchOrLogin<ApiResponse<PageResponse<DmMessage>>>(
    `/api/v1/dm/${partnerId}`,
  );
  return data.content;
}
