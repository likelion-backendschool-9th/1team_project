import Link from "next/link";
import { connection } from "next/server";
import kit from "@/app/_components/ui/kit.module.css";
import { LinkTabs } from "@/app/_components/ui/LinkTabs";
import { getMe } from "@/app/(user)/_lib/api";
import { getPopularKeywords, search, type SearchResult, type SearchSort, type SearchType } from "../_lib/api";
import { SearchResults } from "./SearchResults";
import styles from "./SearchContent.module.css";

type Props = {
  searchParams: Promise<Record<string, string | string[] | undefined>>;
};

const types: { value: SearchType; label: string }[] = [
  { value: "all", label: "전체" },
  { value: "post", label: "블로그" },
  { value: "demo", label: "데모" },
  { value: "user", label: "사용자" },
  { value: "feed", label: "피드" },
];

const sorts: { value: SearchSort; label: string }[] = [
  { value: "relevance", label: "관련도순" },
  { value: "latest", label: "최신순" },
];

function first(value: string | string[] | undefined) {
  return Array.isArray(value) ? value[0] : value;
}

function parsePage(value: string | undefined) {
  const page = Number(value);
  return Number.isSafeInteger(page) && page > 0 ? page : 1;
}

function href(q: string, type: SearchType, sort: SearchSort = "relevance", page = 1, searchId?: string) {
  const query = new URLSearchParams({ q });
  if (type !== "all") query.set("type", type);
  if (sort !== "relevance") query.set("sort", sort);
  if (page > 1) query.set("page", String(page));
  if (page > 1 && searchId) query.set("searchId", searchId);
  return `/search?${query}`;
}

function totalOf(result: SearchResult) {
  return result.posts.total + result.demos.total + result.users.total + result.feeds.total;
}

function countOf(result: SearchResult, type: SearchType) {
  const sections = { post: result.posts, demo: result.demos, user: result.users, feed: result.feeds };
  return type === "all" ? totalOf(result) : sections[type].total;
}

export async function SearchContent({ searchParams }: Props) {
  await connection();
  const params = await searchParams;
  const q = first(params.q)?.trim().slice(0, 50) ?? "";
  const type = types.find((item) => item.value === first(params.type))?.value ?? "all";
  const sort = sorts.find((item) => item.value === first(params.sort))?.value ?? "relevance";
  const page = parsePage(first(params.page));
  const searchId = first(params.searchId)?.slice(0, 36);
  const popular = await getPopularKeywords();

  if (!q)
    return (
      <section className={kit.card} aria-labelledby="popular-title">
        <h2 id="popular-title" className={kit.cardTitle}>
          🔥 인기 검색어
        </h2>
        <ol className={kit.chips} style={{ marginTop: 14 }}>
          {popular.map((keyword, index) => (
            <li key={keyword}>
              <Link href={href(keyword, "all")} className={kit.btn}>
                <span className={kit.faint}>{index + 1}</span> {keyword}
              </Link>
            </li>
          ))}
        </ol>
      </section>
    );

  const [outcome, me] = await Promise.all([search(q, type, sort, page, searchId), getMe()]);
  if ("error" in outcome)
    return (
      <p role="alert" className={kit.empty}>
        {outcome.error}
      </p>
    );

  const { result } = outcome;
  const total = totalOf(result);
  const suggestion = result.suggestedQuery;

  return (
    <div className={kit.stack}>
      <p role="status" className={kit.muted}>
        <strong>“{q}”</strong> 검색 결과 {total}건
      </p>
      {suggestion && (
        <p className={kit.muted}>
          {result.suggestionType === "KEYBOARD" ? "한/영 전환을 잊으셨나요? " : "이것을 찾으셨나요? "}
          <Link href={href(suggestion, type, sort)} className={kit.more}>
            {suggestion}
          </Link>
        </p>
      )}
      {result.correctedQuery && (
        <p className={kit.muted}>
          검색 결과가 없어서 <strong>“{result.correctedQuery}”</strong>(으)로 찾은 결과를 보여 드려요.
        </p>
      )}
      {result.relaxedMatch && <p className={kit.muted}>정확히 일치하는 결과가 없어서 일부만 일치하는 결과를 보여 드려요.</p>}
      <LinkTabs
        label="검색 대상"
        variant="line"
        items={types.map((item) => ({
          href: href(q, item.value, sort),
          label: item.label,
          active: item.value === type,
          // 개별 탭에서는 다른 대상의 건수를 모르므로 지금 탭의 건수만 보여 준다
          count: type === "all" || item.value === type ? countOf(result, item.value) : undefined,
        }))}
      />
      <form action="/search" method="get" className={styles.sortForm}>
        <input type="hidden" name="q" value={q} />
        {type !== "all" && <input type="hidden" name="type" value={type} />}
        <label htmlFor="search-sort" className={kit.label}>
          정렬
        </label>
        <select id="search-sort" name="sort" defaultValue={sort} className={`${kit.select} ${styles.sortSelect}`}>
          {sorts.map((item) => (
            <option key={item.value} value={item.value}>
              {item.label}
            </option>
          ))}
        </select>
        <button type="submit" className={kit.btn}>
          적용
        </button>
      </form>
      {total === 0 && (
        <p className={kit.empty}>
          검색 결과가 없어요. 다른 키워드로 찾아보세요.
          <br />
          {popular.slice(0, 4).map((keyword) => (
            <Link key={keyword} href={href(keyword, "all")} className={kit.more} style={{ margin: "8px 6px 0" }}>
              #{keyword}
            </Link>
          ))}
        </p>
      )}
      <SearchResults result={result} type={type} query={q} canHide={Boolean(me)} tabHref={(target) => href(q, target, sort)} />
      {type !== "all" && result.totalPages > 1 && (
        <nav aria-label="검색 결과 페이지" className={styles.pages}>
          {page > 1 && (
            <Link href={href(q, type, sort, page - 1, result.searchId)} className={kit.btn}>
              이전
            </Link>
          )}
          <span className={kit.faint}>
            {page} / {result.totalPages}
          </span>
          {page < result.totalPages && (
            <Link href={href(q, type, sort, page + 1, result.searchId)} className={kit.btn}>
              다음
            </Link>
          )}
        </nav>
      )}
    </div>
  );
}
