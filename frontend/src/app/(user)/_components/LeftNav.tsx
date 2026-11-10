import { connection } from "next/server";
import { Suspense } from "react";
import { getArticleRanking } from "../blog/_lib/api";
import { getNews } from "../(feed)/news/_lib/api";
import { Brand } from "./Brand";
import { FeedSideCard } from "./FeedSideCard";
import { NavLinks } from "./NavLinks";
import styles from "./LeftNav.module.css";

const newsCategoryLabels: Record<string, string> = {
  frontend: "프론트엔드",
  backend: "백엔드",
  ai: "AI",
  security: "보안",
  game: "게임",
  etc: "기타",
};

async function FeedSideSlot() {
  await connection();
  // 피드에는 인기 블로그 글(좋아요 순)을, 뉴스에는 카테고리를 보여 준다. (이벤트 API는 아직 없다)
  const [ranking, news] = await Promise.all([getArticleRanking(), getNews(undefined, undefined)]);
  const seen = new Set<string>();
  const order = Object.keys(newsCategoryLabels);
  const categories = news.flatMap((item) => {
    if (!item.category || seen.has(item.category)) return [];
    seen.add(item.category);
    return [{ value: item.category, label: newsCategoryLabels[item.category] ?? item.category }];
  });
  categories.sort((a, b) => {
    const left = order.indexOf(a.value);
    const right = order.indexOf(b.value);
    return (left === -1 ? order.length : left) - (right === -1 ? order.length : right);
  });

  return (
    <FeedSideCard
      posts={ranking.likes.map((entry) => ({ id: entry.postId, title: entry.title, score: entry.score }))}
      categories={categories}
    />
  );
}

export function LeftNav() {
  return (
    <aside className={styles.nav} aria-label="사이드바">
      <Brand />
      <NavLinks />
      <Suspense fallback={null}>
        <FeedSideSlot />
      </Suspense>
      <p className={styles.copy}>© 2026 찍먹 dipsource</p>
    </aside>
  );
}
