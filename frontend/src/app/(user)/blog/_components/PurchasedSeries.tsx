import Link from "next/link";
import { Avatar } from "@/app/_components/ui/Avatar";
import { ChevronRightIcon } from "@/app/_components/ui/Icons";
import type { SeriesPurchase } from "@/app/_lib/types/post";
import styles from "./PurchasedSeries.module.css";

type Props = {
  purchases: SeriesPurchase[];
};

/** 블로그 오른쪽의 내가 산 시리즈 (최근에 산 순, 최대 5개) */
export function PurchasedSeries({ purchases }: Props) {
  return (
    <section className={styles.card} aria-labelledby="purchased-title">
      <div className={styles.header}>
        <h2 id="purchased-title" className={styles.title}>
          구매한 시리즈
        </h2>
        <Link href="/my/subscription" className={styles.more}>
          전체 보기
          <ChevronRightIcon />
        </Link>
      </div>
      {purchases.length === 0 && <p className={styles.empty}>아직 산 시리즈가 없어요.</p>}
      <ul className={styles.list}>
        {purchases.slice(0, 5).map(({ series }) => (
          <li key={series.id}>
            <Link href={`/blog/series/${series.id}`} className={styles.row}>
              <Avatar name={series.author.nickname} size={40} tone="light" />
              <span className={styles.info}>
                <span className={styles.name}>{series.title}</span>
                <span className={styles.series}>
                  @{series.author.nickname} · {series.status === "completed" ? "완결" : "연재 중"} {series.episodeCount}화
                </span>
              </span>
            </Link>
          </li>
        ))}
      </ul>
    </section>
  );
}
