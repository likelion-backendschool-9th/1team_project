import Link from "next/link";
import { SearchIcon } from "@/app/_components/ui/Icons";
import type { PostCategory, PostSort } from "@/app/_lib/types/post";
import { blogHref, categories } from "../_lib/query";
import styles from "./BlogSearch.module.css";

type Props = {
  category: PostCategory | undefined;
  sort: PostSort;
  q: string | undefined;
};

export function BlogSearch({ category, sort, q }: Props) {
  const showAll = !category && sort !== "popular";
  const showPopular = !category && sort === "popular";

  return (
    <section className={styles.card} aria-label="블로그 검색과 분류">
      <form role="search" action="/blog" method="get" className={styles.form}>
        <label htmlFor="blog-search" className="sr-only">
          블로그 글 검색
        </label>
        <SearchIcon className={styles.icon} />
        <input
          id="blog-search"
          name="q"
          type="search"
          defaultValue={q}
          maxLength={50}
          placeholder="관심 있는 개발 소스, 연재 시리즈, 스택을 검색해보세요 (예: Next.js, Kafka)"
          autoComplete="off"
          className={styles.input}
        />
        {category && <input type="hidden" name="category" value={category} />}
        <button type="submit" className={styles.submit}>
          검색
        </button>
      </form>
      <nav aria-label="블로그 분류">
        <ul className={styles.chips}>
          <li>
            <Link
              href={blogHref({ q })}
              className={`${styles.chip} ${showAll ? styles.active : ""}`}
              aria-current={showAll ? "page" : undefined}
            >
              전체보기
            </Link>
          </li>
          <li>
            <Link
              href={blogHref({ q, sort: "popular" })}
              className={`${styles.chip} ${styles.hot} ${showPopular ? styles.active : ""}`}
              aria-current={showPopular ? "page" : undefined}
            >
              <span aria-hidden="true">✨</span>
              실시간 인기 연재
            </Link>
          </li>
          {categories.map((item) => (
            <li key={item.value}>
              <Link
                href={blogHref({ q, sort, category: item.value })}
                className={`${styles.chip} ${category === item.value ? styles.active : ""}`}
                aria-current={category === item.value ? "page" : undefined}
              >
                {item.label}
              </Link>
            </li>
          ))}
        </ul>
      </nav>
    </section>
  );
}
