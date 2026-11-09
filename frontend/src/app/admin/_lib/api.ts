import "server-only";
import { mockEvents } from "@/app/_lib/mockEvents";
import type { DevEvent } from "@/app/_lib/types/event";
import { apiFetch, ApiRequestError, hasBackend, isMockMode, type ApiResponse, type PageResponse } from "@/app/_lib/api/client";
import {
  mockAdminSubscriptions,
  mockAdminUsers,
  mockCoinTx,
  mockContent,
  mockReports,
  mockRevenue,
  mockSanctions,
  mockSettlements,
  mockSignups,
  mockSuspicious,
  type AdminUser,
  type ContentKind,
  type ContentRow,
  type Report,
  type Sanction,
} from "./mock";

export const PAGE_SIZE = 10;

async function get<T>(path: `/api/v1/${string}`, mock: T): Promise<T> {
  if (isMockMode) return mock;
  const { data } = await apiFetch<ApiResponse<T>>(path);
  return data;
}

export type Dashboard = {
  userCount: number;
  newUsersToday: number;
  demoUploadsWeek: number;
  pendingReports: number;
  revenueMonth: number;
  signups: number[];
};

export async function getDashboard(): Promise<Dashboard> {
  if (hasBackend) {
    const { data } = await apiFetch<ApiResponse<Dashboard>>("/api/v1/admin/dashboard");
    return data;
  }
  const latest = mockRevenue.at(-2)!;
  return {
    userCount: 4_812,
    newUsersToday: mockSignups.at(-1)!,
    demoUploadsWeek: 186,
    pendingReports: mockReports.filter((report) => report.status === "pending").length,
    revenueMonth: latest.subscription + latest.plus + latest.chips,
    signups: mockSignups,
  };
}

export async function getUsers(query: { q?: string; status?: string; page: number }): Promise<PageResponse<AdminUser>> {
  if (hasBackend) {
    const search = new URLSearchParams({ page: String(query.page - 1), size: String(PAGE_SIZE) });
    if (query.q) search.set("q", query.q);
    if (query.status) search.set("status", query.status);
    const { data } = await apiFetch<ApiResponse<PageResponse<AdminUser>>>(`/api/v1/admin/users?${search}`);
    return data;
  }
  const filtered = mockAdminUsers.filter(
    (user) =>
      (!query.q || `${user.nickname} ${user.email}`.includes(query.q)) &&
      (!query.status || user.status === query.status),
  );
  return {
    content: filtered.slice((query.page - 1) * PAGE_SIZE, query.page * PAGE_SIZE),
    page: query.page - 1,
    size: PAGE_SIZE,
    totalElements: filtered.length,
    totalPages: Math.max(1, Math.ceil(filtered.length / PAGE_SIZE)),
  };
}

export async function getUser(userId: number) {
  if (!hasBackend) {
    const user = mockAdminUsers.find((item) => item.id === userId) ?? null;
    return user
      ? { user, sanctions: user.status === "suspended" ? mockSanctions : mockSanctions.slice(0, 1) }
      : null;
  }
  try {
    const { data } = await apiFetch<ApiResponse<{ user: AdminUser; sanctions: Sanction[] }>>(
      `/api/v1/admin/users/${userId}`,
    );
    return data;
  } catch (error) {
    if (error instanceof ApiRequestError && error.status === 404) return null;
    throw error;
  }
}

export async function getContent(
  kind: ContentKind,
  query: { q?: string; page: number },
): Promise<PageResponse<ContentRow>> {
  if (hasBackend) {
    const search = new URLSearchParams({ page: String(query.page - 1), size: String(PAGE_SIZE) });
    if (query.q) search.set("q", query.q);
    const { data } = await apiFetch<ApiResponse<PageResponse<ContentRow>>>(`/api/v1/admin/${kind}?${search}`);
    return data;
  }
  const filtered = mockContent[kind].filter(
    (row) => !query.q || `${row.title} ${row.author}`.includes(query.q),
  );
  return {
    content: filtered.slice((query.page - 1) * PAGE_SIZE, query.page * PAGE_SIZE),
    page: query.page - 1,
    size: PAGE_SIZE,
    totalElements: filtered.length,
    totalPages: Math.max(1, Math.ceil(filtered.length / PAGE_SIZE)),
  };
}

export const REPORT_TYPES = [
  { value: "demos", label: "데모" },
  { value: "posts", label: "블로그" },
  { value: "feeds", label: "피드" },
  { value: "comments", label: "댓글" },
  { value: "guestbooks", label: "방명록" },
  { value: "users", label: "회원" },
] as const;

export async function getReports(query: {
  status?: string;
  type?: string;
  page: number;
}): Promise<PageResponse<Report>> {
  if (hasBackend) {
    const search = new URLSearchParams({ page: String(query.page - 1), size: String(PAGE_SIZE) });
    if (query.status) search.set("status", query.status);
    if (query.type) search.set("type", query.type);
    const { data } = await apiFetch<ApiResponse<PageResponse<Report>>>(`/api/v1/admin/reports?${search}`);
    return data;
  }
  const typeLabel = REPORT_TYPES.find((item) => item.value === query.type)?.label;
  const order = { pending: 0, resolved: 1, rejected: 2 };
  const filtered = mockReports
    .filter((report) => (!query.status || report.status === query.status) && (!typeLabel || report.targetType === typeLabel))
    .sort((a, b) => order[a.status] - order[b.status] || b.createdAt.localeCompare(a.createdAt));
  return {
    content: filtered.slice((query.page - 1) * PAGE_SIZE, query.page * PAGE_SIZE),
    page: query.page - 1,
    size: PAGE_SIZE,
    totalElements: filtered.length,
    totalPages: Math.max(1, Math.ceil(filtered.length / PAGE_SIZE)),
  };
}

export async function getReport(reportId: number): Promise<Report | null> {
  if (hasBackend) {
    try {
      const { data } = await apiFetch<ApiResponse<Report>>(`/api/v1/admin/reports/${reportId}`);
      return data;
    } catch (error) {
      if (error instanceof ApiRequestError && error.status === 404) return null;
      throw error;
    }
  }
  return mockReports.find((report) => report.id === reportId) ?? null;
}

export const getSubscriptions = () => get("/api/v1/admin/subscriptions", mockAdminSubscriptions);
export async function getRevenue(from?: string, to?: string) {
  if (!isMockMode) {
    const query = new URLSearchParams();
    if (from) query.set("from", from);
    if (to) query.set("to", to);
    const { data } = await apiFetch<ApiResponse<typeof mockRevenue>>(`/api/v1/admin/revenue?${query}`);
    return data;
  }
  return mockRevenue.filter((row) => (!from || row.month >= from) && (!to || row.month <= to));
}
export async function getSettlements(): Promise<typeof mockSettlements> {
  if (!hasBackend) return mockSettlements;
  // 정산 API는 백엔드에 있어서 MOCK_EXCEPT_DM이어도 실서버를 쓴다. 최근 100건만 보여 준다.
  const { data } = await apiFetch<ApiResponse<PageResponse<(typeof mockSettlements)[number]>>>(
    "/api/v1/admin/settlements?size=100",
  );
  return data.content;
}
export const getCoinTx = () => get("/api/v1/admin/coins/purchases", mockCoinTx);
export const getSuspicious = () => get("/api/v1/admin/coins/suspicious", mockSuspicious);

export async function getAdminEvents(): Promise<DevEvent[]> {
  if (isMockMode) return mockEvents;
  const { data } = await apiFetch<ApiResponse<PageResponse<DevEvent>>>("/api/v1/events");
  return data.content;
}
