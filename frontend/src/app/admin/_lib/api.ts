import "server-only";
import { mockEvents } from "@/app/_lib/mockEvents";
import { apiFetch, isMockMode, type ApiResponse, type PageResponse } from "@/app/_lib/api/client";
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

export function getDashboard(): Promise<Dashboard> {
  const latest = mockRevenue.at(-2)!;
  return get("/api/v1/admin/dashboard", {
    userCount: 4_812,
    newUsersToday: mockSignups.at(-1)!,
    demoUploadsWeek: 186,
    pendingReports: mockReports.filter((report) => report.status === "pending").length,
    revenueMonth: latest.subscription + latest.plus + latest.chips,
    signups: mockSignups,
  });
}

export async function getUsers(query: { q?: string; status?: string; page: number }): Promise<PageResponse<AdminUser>> {
  if (!isMockMode) {
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
  const user = await get<AdminUser | null>(
    `/api/v1/admin/users/${userId}`,
    mockAdminUsers.find((item) => item.id === userId) ?? null,
  );
  return user ? { user, sanctions: user.status === "suspended" ? mockSanctions : mockSanctions.slice(0, 1) } : null;
}

export async function getContent(kind: ContentKind, q?: string): Promise<ContentRow[]> {
  if (!isMockMode) {
    const { data } = await apiFetch<ApiResponse<PageResponse<ContentRow>>>(
      `/api/v1/admin/${kind}?${new URLSearchParams(q ? { q } : {})}`,
    );
    return data.content;
  }
  return mockContent[kind].filter((row) => !q || `${row.title} ${row.author}`.includes(q));
}

export const REPORT_TYPES = [
  { value: "demos", label: "데모" },
  { value: "posts", label: "블로그" },
  { value: "feeds", label: "피드" },
  { value: "comments", label: "댓글" },
  { value: "guestbooks", label: "방명록" },
] as const;

export async function getReports(status?: string, type?: string): Promise<Report[]> {
  const query = new URLSearchParams();
  if (status) query.set("status", status);
  if (type) query.set("type", type);
  const reports = await get(`/api/v1/admin/reports?${query}`, mockReports);
  const typeLabel = REPORT_TYPES.find((item) => item.value === type)?.label;
  const order = { pending: 0, resolved: 1, rejected: 2 };
  return reports
    .filter((report) => (!status || report.status === status) && (!typeLabel || report.targetType === typeLabel))
    .sort((a, b) => order[a.status] - order[b.status] || b.createdAt.localeCompare(a.createdAt));
}

export async function getReport(reportId: number): Promise<Report | null> {
  return (await getReports()).find((report) => report.id === reportId) ?? null;
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
export const getSettlements = () => get("/api/v1/admin/settlements", mockSettlements);
export const getCoinTx = () => get("/api/v1/admin/coins/purchases", mockCoinTx);
export const getSuspicious = () => get("/api/v1/admin/coins/suspicious", mockSuspicious);

export const getAdminEvents = () => get("/api/v1/events", mockEvents);
