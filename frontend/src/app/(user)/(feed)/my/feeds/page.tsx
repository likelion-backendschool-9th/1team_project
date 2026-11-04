import type { Metadata } from "next";
import Link from "next/link";
import { connection } from "next/server";
import { Suspense } from "react";
import kit from "@/app/_components/ui/kit.module.css";
import { getMe } from "@/app/(user)/_lib/api";
import { FeedCard } from "../../_components/FeedCard";
import { getMyFeeds, getServerTime } from "./_lib/api";
import styles from "../../main/_components/MainContent.module.css";

export const metadata: Metadata = { title: "내 피드", robots: { index: false } };

async function MyFeedsContent() {
  await connection();
  const [me, now] = await Promise.all([getMe(), getServerTime()]);

  if (!me) {
    return (
      <p className={kit.empty}>
        내 피드를 보려면 <Link href="/login?redirect=/my/feeds">로그인</Link>이 필요해요.
      </p>
    );
  }

  const feeds = await getMyFeeds();

  if (feeds.length === 0) {
    return (
      <p className={styles.empty}>
        아직 작성한 피드가 없어요. <Link href="/main">피드 작성하러 가기</Link>
      </p>
    );
  }

  return (
    <ul className={styles.list}>
      {feeds.map((feed) => (
        <li key={feed.id}>
          <FeedCard feed={feed} myId={me.id} now={now} />
        </li>
      ))}
    </ul>
  );
}

export default function MyFeedsPage() {
  return (
    <>
      <h1 className={kit.pageTitle}>내 피드</h1>
      <p className={kit.faint} style={{ marginBottom: 20 }}>
        내가 작성한 피드를 모아서 봐요. 수정·삭제는 각 카드의 더보기 메뉴에서 할 수 있어요.
      </p>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <MyFeedsContent />
      </Suspense>
    </>
  );
}
