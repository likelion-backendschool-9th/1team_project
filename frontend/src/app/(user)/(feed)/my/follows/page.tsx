import type { Metadata } from "next";
import Link from "next/link";
import { connection } from "next/server";
import { Suspense } from "react";
import { Avatar } from "@/app/_components/ui/Avatar";
import kit from "@/app/_components/ui/kit.module.css";
import { LinkTabs } from "@/app/_components/ui/LinkTabs";
import { Pagination } from "@/app/_components/ui/Pagination";
import { DmOpenButton } from "@/app/(user)/_components/DmOpenButton";
import { FollowButton } from "../../_components/FollowButton";
import { getFollowers, getFollowings } from "../_lib/api";
import styles from "../_components/My.module.css";

export const metadata: Metadata = { title: "팔로우", robots: { index: false } };

type SearchParams = Promise<{ tab?: string | string[]; page?: string | string[] }>;

function parsePage(value: string | string[] | undefined) {
  const page = Number(value);
  return Number.isSafeInteger(page) && page > 0 ? page : 1;
}

async function FollowsContent({ searchParams }: { searchParams: SearchParams }) {
  await connection();
  const params = await searchParams;
  const tab = params.tab === "followers" ? "followers" : "followings";
  const page = parsePage(params.page);
  // 보고 있는 탭은 해당 페이지를, 다른 탭은 숫자만 필요해서 1명만 받는다.
  const [followings, followers] = await Promise.all([
    tab === "followings" ? getFollowings(page) : getFollowings(1, 1),
    tab === "followers" ? getFollowers(page) : getFollowers(1, 1),
  ]);
  const current = tab === "followers" ? followers : followings;
  const users = current.users;
  const tabHref = tab === "followers" ? "/my/follows?tab=followers" : "/my/follows";

  return (
    <>
      <LinkTabs
        label="팔로우 목록"
        variant="line"
        items={[
          { href: "/my/follows", label: "팔로잉", active: tab === "followings", count: followings.total },
          { href: "/my/follows?tab=followers", label: "팔로워", active: tab === "followers", count: followers.total },
        ]}
      />
      <section className={kit.card} aria-label={tab === "followers" ? "팔로워" : "팔로잉"}>
        {users.length === 0 ? (
          <p className={kit.empty}>아직 아무도 없어요.</p>
        ) : (
          <ul className={kit.list}>
            {users.map((user) => (
              <li key={user.id} className={kit.listItem}>
                <Link href={`/mini-home/${user.id}`} className={styles.person}>
                  <Avatar name={user.nickname} size={44} />
                  <span className={kit.grow}>
                    <strong>{user.nickname}</strong>
                    {user.followsMe && tab === "followings" && <span className={kit.pill} style={{ marginLeft: 6 }}>맞팔</span>}
                    <span className={kit.faint} style={{ display: "block" }}>
                      {user.techStacks.join(", ") || "기술 스택 미입력"}
                    </span>
                  </span>
                </Link>
                <DmOpenButton user={user} className={`${kit.btnGhost} ${kit.sm}`}>
                  DM
                </DmOpenButton>
                <FollowButton userId={user.id} nickname={user.nickname} following={user.following} />
              </li>
            ))}
          </ul>
        )}
      </section>
      <Pagination
        page={current.page}
        totalPages={current.totalPages}
        hrefFor={(value) => (value === 1 ? tabHref : `${tabHref}${tab === "followers" ? "&" : "?"}page=${value}`)}
      />
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
