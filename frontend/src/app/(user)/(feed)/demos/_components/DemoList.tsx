import { connection } from "next/server";
import kit from "@/app/_components/ui/kit.module.css";
import { LinkTabs } from "@/app/_components/ui/LinkTabs";
import { Pagination } from "@/app/_components/ui/Pagination";
import type { DemoSort } from "@/app/_lib/types/demo";
import { getDemoPage, getDemoTags } from "../_lib/api";
import { DemoCard } from "./DemoCard";
import styles from "./DemoList.module.css";

type Props = {
  searchParams: Promise<Record<string, string | string[] | undefined>>;
};

const sorts: { value: DemoSort; label: string }[] = [
  { value: "trending", label: "지금 뜨는" },
  { value: "latest", label: "최신순" },
  { value: "popular", label: "좋아요순" },
];

function first(value: string | string[] | undefined) {
  return Array.isArray(value) ? value[0] : value;
}

function href(tag: string | undefined, sort: DemoSort, page = 1) {
  const query = new URLSearchParams();
  if (tag) query.set("tag", tag);
  if (sort !== "trending") query.set("sort", sort);
  if (page > 1) query.set("page", String(page));
  const search = query.toString();
  return search ? `/demos?${search}` : "/demos";
}

export async function DemoList({ searchParams }: Props) {
  await connection();
  const params = await searchParams;
  const tags = await getDemoTags();
  const rawTag = first(params.tag);
  const tag = rawTag && tags.includes(rawTag) ? rawTag : undefined;
  const sort = sorts.find((item) => item.value === first(params.sort))?.value ?? "trending";
  const requested = Math.max(1, Math.floor(Number(first(params.page))) || 1);
  const { items: demos, page, totalPages, total } = await getDemoPage(tag, sort, requested);

  return (
    <div className={kit.stack}>
      <LinkTabs
        label="데모 정렬"
        variant="line"
        items={sorts.map((item) => ({
          href: href(tag, item.value),
          label: item.label,
          active: item.value === sort,
        }))}
      />
      <LinkTabs
        label="태그 필터"
        items={[
          { href: href(undefined, sort), label: "전체", active: !tag },
          ...tags.map((name) => ({ href: href(name, sort), label: `#${name}`, active: tag === name })),
        ]}
      />
      <p className={kit.faint} role="status">
        데모 {total}개{totalPages > 1 && ` · ${page}/${totalPages} 페이지`}
      </p>
      {demos.length === 0 ? (
        <p className={kit.empty}>{tag ? "이 태그의 데모가 아직 없어요." : "아직 올라온 데모가 없어요. 첫 데모를 올려 보세요."}</p>
      ) : (
        <ul className={styles.grid}>
          {demos.map((demo) => (
            <li key={demo.id}>
              <DemoCard demo={demo} />
            </li>
          ))}
        </ul>
      )}
      {/* 정렬·태그는 유지한 채 페이지 번호만 바꾼다 */}
      <Pagination page={page} totalPages={totalPages} hrefFor={(target) => href(tag, sort, target)} />
    </div>
  );
}
