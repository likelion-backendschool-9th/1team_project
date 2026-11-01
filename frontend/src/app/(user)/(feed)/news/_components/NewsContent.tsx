import { connection } from "next/server";
import { SearchIcon } from "@/app/_components/ui/Icons";
import kit from "@/app/_components/ui/kit.module.css";
import { LinkTabs } from "@/app/_components/ui/LinkTabs";
import { formatDateTime } from "@/app/_lib/format";
import { getNews } from "../_lib/api";
import styles from "./News.module.css";

type Props = {
  searchParams: Promise<Record<string, string | string[] | undefined>>;
};

const categories = [
  { value: "frontend", label: "프론트엔드" },
  { value: "backend", label: "백엔드" },
  { value: "ai", label: "AI" },
  { value: "security", label: "보안" },
  { value: "game", label: "게임" },
];

function first(value: string | string[] | undefined) {
  return Array.isArray(value) ? value[0] : value;
}

function href(q: string | undefined, category: string | undefined) {
  const query = new URLSearchParams();
  if (q) query.set("q", q);
  if (category) query.set("category", category);
  const search = query.toString();
  return search ? `/news?${search}` : "/news";
}

export async function NewsContent({ searchParams }: Props) {
  await connection();
  const params = await searchParams;
  const q = first(params.q)?.trim().slice(0, 50) || undefined;
  const category = categories.find((item) => item.value === first(params.category))?.value;
  const news = await getNews(q, category);
  const label = (value: string) => categories.find((item) => item.value === value)?.label ?? value;

  return (
    <div className={kit.stack}>
      <form role="search" action="/news" method="get" className={styles.search}>
        <label htmlFor="news-q" className="sr-only">
          뉴스 검색
        </label>
        <SearchIcon className={styles.icon} />
        <input id="news-q" name="q" type="search" defaultValue={q} placeholder="키워드로 뉴스 찾기" className={styles.input} />
        {category && <input type="hidden" name="category" value={category} />}
        <button type="submit" className={kit.btnPrimary}>
          검색
        </button>
      </form>
      <LinkTabs
        label="뉴스 분야"
        items={[
          { href: href(q, undefined), label: "전체", active: !category },
          ...categories.map((item) => ({ href: href(q, item.value), label: item.label, active: category === item.value })),
        ]}
      />
      {news.length === 0 ? (
        <p className={kit.empty}>조건에 맞는 뉴스가 없어요.</p>
      ) : (
        <ul className={styles.list}>
          {news.map((item, index) => (
            <li key={item.id}>
              <a href={item.url} target="_blank" rel="noopener noreferrer" className={`${kit.card} ${styles.item}`}>
                {index === 0 && !q && !category && <span className={kit.pillDanger}>HOT</span>}
                <h2 className={styles.title}>{item.title}</h2>
                <p className={kit.muted}>{item.summary}</p>
                <p className={styles.meta}>
                  <span className={kit.pill}>{label(item.category)}</span>
                  {item.source} · <time dateTime={item.publishedAt}>{formatDateTime(item.publishedAt)}</time>
                  <span className="sr-only">(새 창)</span>
                </p>
              </a>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
