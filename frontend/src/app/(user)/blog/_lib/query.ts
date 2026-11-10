import type { PostCategory, PostSort } from "@/app/_lib/types/post";

export const categories: { value: PostCategory; label: string }[] = [
  { value: "frontend", label: "프론트엔드" },
  { value: "backend", label: "백엔드 & 인프라" },
  { value: "ai", label: "AI & 데이터" },
  { value: "mobile", label: "모바일" },
  { value: "career", label: "커리어" },
  { value: "game", label: "게임" },
  { value: "uxui", label: "UX/UI" },
];

/** 유료 글 본문 최소 글자 수 (앞뒤 공백 제외). 백엔드 PostServiceImpl과 같은 값 */
export const PAID_MIN_LENGTH = 1000;

export const sorts: { value: PostSort; label: string }[] = [
  { value: "latest", label: "최신순" },
  { value: "popular", label: "인기순" },
  { value: "recommended", label: "추천순" },
];

type Param = string | string[] | undefined;

function first(value: Param) {
  return Array.isArray(value) ? value[0] : value;
}

export function parseCategory(value: Param): PostCategory | undefined {
  const raw = first(value);
  return categories.find((category) => category.value === raw)?.value;
}

export function parseSort(value: Param): PostSort {
  const raw = first(value);
  return sorts.find((sort) => sort.value === raw)?.value ?? "latest";
}

export function parseKeyword(value: Param): string | undefined {
  const raw = first(value)?.trim().slice(0, 50);
  return raw || undefined;
}

// 기본값은 URL에서 뺀다.
export function blogHref(params: {
  category?: PostCategory;
  sort?: PostSort;
  q?: string;
}) {
  const query = new URLSearchParams();
  if (params.q) query.set("q", params.q);
  if (params.category) query.set("category", params.category);
  if (params.sort && params.sort !== "latest") query.set("sort", params.sort);
  const search = query.toString();
  return search ? `/blog?${search}` : "/blog";
}
