import type { Metadata } from "next";
import Link from "next/link";
import { connection } from "next/server";
import { Suspense } from "react";
import kit from "@/app/_components/ui/kit.module.css";
import { LinkTabs } from "@/app/_components/ui/LinkTabs";
import { formatDate } from "@/app/_lib/format";
import { getBookmarks } from "../_lib/api";
import type { BookmarkType } from "../_lib/mock";

export const metadata: Metadata = { title: "북마크", robots: { index: false } };

const types: { value: BookmarkType; label: string; icon: string }[] = [
  { value: "feed", label: "피드", icon: "💬" },
  { value: "post", label: "블로그 글", icon: "📝" },
  { value: "demo", label: "데모", icon: "🧪" },
];

async function BookmarksContent({ searchParams }: { searchParams: Promise<Record<string, string | string[] | undefined>> }) {
  await connection();
  const raw = (await searchParams).type;
  const type = types.find((item) => item.value === raw)?.value;
  const bookmarks = await getBookmarks(type);
  const icon = (value: BookmarkType) => types.find((item) => item.value === value)?.icon;

  return (
    <>
      <LinkTabs
        label="북마크 종류"
        variant="line"
        items={[
          { href: "/my/bookmarks", label: "전체", active: !type },
          ...types.map((item) => ({ href: `/my/bookmarks?type=${item.value}`, label: item.label, active: item.value === type })),
        ]}
      />
      <section className={kit.card} aria-label="북마크 목록">
        {bookmarks.length === 0 ? (
          <p className={kit.empty}>저장한 항목이 없어요.</p>
        ) : (
          <ul className={kit.list}>
            {bookmarks.map((item) => (
              <li key={item.id}>
                <Link href={item.href} className={kit.listItem}>
                  <span aria-hidden="true">{icon(item.type)}</span>
                  <span className={kit.grow}>
                    <span className={`${kit.strong} ${kit.ellipsis}`} style={{ display: "block" }}>
                      {item.title}
                    </span>
                    <span className={kit.faint}>
                      {item.author} · {formatDate(item.savedAt)} 저장
                    </span>
                  </span>
                </Link>
              </li>
            ))}
          </ul>
        )}
      </section>
    </>
  );
}

export default function BookmarksPage({ searchParams }: PageProps<"/my/bookmarks">) {
  return (
    <>
      <h1 className={kit.pageTitle}>북마크</h1>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <BookmarksContent searchParams={searchParams} />
      </Suspense>
    </>
  );
}
