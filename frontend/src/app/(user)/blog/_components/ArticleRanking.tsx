import Link from "next/link";
import { ChevronRightIcon } from "@/app/_components/ui/Icons";
import { getArticleRanking } from "../_lib/api";
import { ArticleRankingTabs } from "./ArticleRankingTabs";
import styles from "./ArticleRanking.module.css";

export async function ArticleRanking() {
  const ranking = await getArticleRanking();

  return (
    <>
      <div className={styles.header}>
        <h2 id="article-ranking-title" className={styles.title}>
          오늘의 아티클 순위
        </h2>
        <Link href="/rankings?type=blog&period=weekly" className={styles.more}>
          이번 주
          <ChevronRightIcon />
        </Link>
      </div>
      <ArticleRankingTabs ranking={ranking} />
    </>
  );
}
