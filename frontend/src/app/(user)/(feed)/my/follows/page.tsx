import type { Metadata } from "next";
import { connection } from "next/server";
import { Suspense } from "react";
import kit from "@/app/_components/ui/kit.module.css";
import { LinkTabs } from "@/app/_components/ui/LinkTabs";
import { getFollowers, getFollowings } from "../_lib/api";
import { FollowList } from "./_components/FollowList";
import { FollowSearch } from "./_components/FollowSearch";
import list from "./_components/FollowList.module.css";
import { followsHref, parseFollowQuery, parseFollowTab } from "./_lib/params";

export const metadata: Metadata = { title: "팔로우", robots: { index: false } };

type SearchParams = Promise<{ tab?: string | string[]; q?: string | string[] }>;

async function FollowsContent({ searchParams }: { searchParams: SearchParams }) {
  await connection();
  const params = await searchParams;
  const tab = parseFollowTab(params.tab);
  const q = parseFollowQuery(params.q);

  // 보고 있는 탭은 검색어를 넣은 첫 페이지를, 탭 숫자는 검색과 상관없는 전체 수를 쓴다.
  const [current, followingCount, followerCount] = await Promise.all([
    tab === "followers" ? getFollowers(1, undefined, q) : getFollowings(1, undefined, q),
    getFollowings(1, 1).then((page) => page.total),
    getFollowers(1, 1).then((page) => page.total),
  ]);

  return (
    <>
      <LinkTabs
        label="팔로우 목록"
        variant="line"
        items={[
          // 검색 중에 탭을 바꿔도 검색어를 유지한다.
          { href: followsHref("followings", q), label: "팔로잉", active: tab === "followings", count: followingCount },
          { href: followsHref("followers", q), label: "팔로워", active: tab === "followers", count: followerCount },
        ]}
      />
      <FollowSearch key={tab} tab={tab} q={q} />
      {q && (
        <p className={`${kit.faint} ${list.result}`}>
          &apos;{q}&apos; 검색 결과 {current.total}명
        </p>
      )}
      {/* 탭이나 검색어가 바뀌면 이어 붙이던 목록을 처음부터 다시 시작한다. */}
      <FollowList key={`${tab}:${q}`} tab={tab} q={q} initial={current} />
    </>
  );
}

export default function FollowsPage({ searchParams }: PageProps<"/my/follows">) {
  return (
    <>
      <h1 className={kit.pageTitle}>팔로우</h1>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <FollowsContent searchParams={searchParams} />
      </Suspense>
    </>
  );
}
