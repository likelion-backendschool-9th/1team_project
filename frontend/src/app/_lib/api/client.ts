import "server-only";
import { cookies } from "next/headers";

const API_ORIGIN = process.env.API_ORIGIN;

export type ApiError = { code: string; message: string };
export type ApiResponse<T> = { data: T };
export type PageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export class ApiRequestError extends Error {
  constructor(public readonly status: number, public readonly error: ApiError) {
    super(error.message);
  }
}

// API_ORIGIN이 없으면 백엔드 연결 전으로 보고 목 데이터를 쓴다.
export const isMockMode = !API_ORIGIN;

export async function apiFetch<T>(
  path: `/api/v1/${string}`,
  init: RequestInit = {},
): Promise<T> {
  const response = await fetch(`${API_ORIGIN}${path}`, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      cookie: (await cookies()).toString(),
      ...init.headers,
    },
    cache: "no-store",
  });
  if (!response.ok)
    throw new ApiRequestError(response.status, await response.json());
  return response.status === 204 ? (undefined as T) : response.json();
}
