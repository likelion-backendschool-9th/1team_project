import Link from "next/link";
import { connection } from "next/server";
import kit from "@/app/_components/ui/kit.module.css";
import { LinkTabs } from "@/app/_components/ui/LinkTabs";
import type { DemoSort } from "@/app/_lib/types/demo";
import { getDemos, getDemoTags } from "../_lib/api";
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

function href(tag: string | undefined, sort: DemoSort) {
  const query = new URLSearchParams();
  if (tag) query.set("tag", tag);
  if (sort !== "trending") query.set("sort", sort);
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
  const { items: demos, nextCursor } = await getDemos(tag, sort, first(params.cursor));

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
        데모 {demos.length}개
      </p>
      {demos.length === 0 ? (
        <p className={kit.empty}>이 태그의 데모가 아직 없어요.</p>
      ) : (
        <ul className={styles.grid}>
          {demos.map((demo) => (
            <li key={demo.id}>
              <DemoCard demo={demo} />
            </li>
          ))}
        </ul>
      )}
      {nextCursor && (
        <Link href={`${href(tag, sort)}${href(tag, sort).includes("?") ? "&" : "?"}cursor=${encodeURIComponent(nextCursor)}`} className={kit.more}>
          다음 데모 보기 →
        </Link>
      )}
    </div>
  );
}
