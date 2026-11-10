import type { Metadata } from "next";
import Link from "next/link";
import { connection } from "next/server";
import { redirect } from "next/navigation";
import { Suspense } from "react";
import kit from "@/app/_components/ui/kit.module.css";
import { getMe } from "@/app/(user)/_lib/api";
import { FeedCard } from "../../_components/FeedCard";
import { getMyReposts } from "./_lib/api";
import { getServerTime } from "../feeds/_lib/api";
import styles from "../../main/_components/MainContent.module.css";

export const metadata: Metadata = { title: "리포스트", robots: { index: false } };

async function MyRepostsContent() {
  await connection();
  const [me, now] = await Promise.all([getMe(), getServerTime()]);

  if (!me) {
    redirect("/login?redirect=/my/reposts");
  }

  const feeds = await getMyReposts();

  if (feeds.length === 0) {
    return (
      <p className={styles.empty}>
        아직 리포스트한 피드가 없어요. <Link href="/">피드 둘러보기</Link>
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

export default function MyRepostsPage() {
  return (
    <>
      <h1 className={kit.pageTitle}>리포스트</h1>
      <p className={kit.faint} style={{ marginBottom: 20 }}>
        내가 리포스트한 피드를 모아서 봐요.
      </p>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <MyRepostsContent />
      </Suspense>
    </>
  );
}
