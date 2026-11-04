import Link from "next/link";
import { connection } from "next/server";
import { Avatar } from "@/app/_components/ui/Avatar";
import kit from "@/app/_components/ui/kit.module.css";
import { LinkTabs } from "@/app/_components/ui/LinkTabs";
import { getPopularKeywords, search, type SearchSort, type SearchType } from "../_lib/api";
import styles from "./SearchContent.module.css";

type Props = {
  searchParams: Promise<Record<string, string | string[] | undefined>>;
};

const types: { value: SearchType; label: string }[] = [
  { value: "all", label: "전체" },
  { value: "user", label: "사람" },
  { value: "demo", label: "데모" },
  { value: "post", label: "글" },
];

const sorts: { value: SearchSort; label: string }[] = [
  { value: "relevance", label: "관련도순" },
  { value: "latest", label: "최신순" },
];

function first(value: string | string[] | undefined) {
  return Array.isArray(value) ? value[0] : value;
}

function href(q: string, type: SearchType, sort: SearchSort = "relevance") {
  const query = new URLSearchParams({ q });
  if (type !== "all") query.set("type", type);
  if (sort !== "relevance") query.set("sort", sort);
  return `/search?${query}`;
}

export async function SearchContent({ searchParams }: Props) {
  await connection();
  const params = await searchParams;
  const q = first(params.q)?.trim().slice(0, 50) ?? "";
  const type = types.find((item) => item.value === first(params.type))?.value ?? "all";
  const sort = sorts.find((item) => item.value === first(params.sort))?.value ?? "relevance";
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

  const result = await search(q, sort);
  const total = result.users.length + result.demos.length + result.posts.length;
  const show = (target: SearchType) => type === "all" || type === target;

  return (
    <div className={kit.stack}>
      <p role="status" className={kit.muted}>
        <strong>“{q}”</strong> 검색 결과 {total}건
      </p>
      <LinkTabs
        label="검색 대상"
        variant="line"
        items={types.map((item) => ({
          href: href(q, item.value, sort),
          label: item.label,
          active: item.value === type,
          count:
            item.value === "user"
              ? result.users.length
              : item.value === "demo"
                ? result.demos.length
                : item.value === "post"
                  ? result.posts.length
                  : total,
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
      {show("user") && result.users.length > 0 && (
        <section className={kit.card} aria-labelledby="result-users">
          <h2 id="result-users" className={kit.cardTitle}>사람</h2>
          <ul className={kit.list}>
            {result.users.map((user) => (
              <li key={user.id}>
                <Link href={`/mini-home/${user.id}`} className={kit.listItem}>
                  <Avatar name={user.nickname} size={40} />
                  <span className={kit.grow}>
                    <strong>{user.nickname}</strong>
                    <span className={kit.faint}> · {user.techStacks.join(", ")}</span>
                  </span>
                  <span className={kit.more}>미니홈피 →</span>
                </Link>
              </li>
            ))}
          </ul>
        </section>
      )}
      {show("demo") && result.demos.length > 0 && (
        <section className={kit.card} aria-labelledby="result-demos">
          <h2 id="result-demos" className={kit.cardTitle}>데모</h2>
          <ul className={kit.list}>
            {result.demos.map((demo) => (
              <li key={demo.id}>
                <Link href={`/demos/${demo.id}`} className={kit.listItem}>
                  <span aria-hidden="true">🧪</span>
                  <span className={kit.grow}>
                    <strong>{demo.title}</strong>
                    <span className={kit.faint}> · {demo.author}</span>
                    <span className={`${kit.muted} ${kit.ellipsis}`} style={{ display: "block", fontSize: 14 }}>
                      {demo.description}
                    </span>
                  </span>
                  <span className={kit.pill}>♥ {demo.likeCount}</span>
                </Link>
              </li>
            ))}
          </ul>
        </section>
      )}
      {show("post") && result.posts.length > 0 && (
        <section className={kit.card} aria-labelledby="result-posts">
          <h2 id="result-posts" className={kit.cardTitle}>글</h2>
          <ul className={kit.list}>
            {result.posts.map((post) => (
              <li key={post.id}>
                <Link href={`/blog/posts/${post.id}`} className={kit.listItem}>
                  <span aria-hidden="true">📝</span>
                  <span className={kit.grow}>
                    <strong>{post.title}</strong>
                    <span className={`${kit.muted} ${kit.ellipsis}`} style={{ display: "block", fontSize: 14 }}>
                      {post.author} · {post.excerpt}
                    </span>
                  </span>
                  <span className={post.price > 0 ? kit.pillAccent : kit.pill}>
                    {post.price > 0 ? `🔒 ${post.price}칩` : "무료"}
                  </span>
                </Link>
              </li>
            ))}
          </ul>
        </section>
      )}
    </div>
  );
}
