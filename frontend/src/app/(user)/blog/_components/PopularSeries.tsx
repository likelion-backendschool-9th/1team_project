import type { PostSeries } from "@/app/_lib/types/post";
import styles from "./PopularSeries.module.css";
import { SeriesCard } from "./SeriesCard";

type Props = {
  series: PostSeries[];
};

export function PopularSeries({ series }: Props) {
  // 시리즈가 하나도 없으면 섹션을 통째로 숨긴다.
  if (series.length === 0) return null;
  return (
    <section aria-labelledby="series-title">
      <div className={styles.header}>
        <div>
          <h2 id="series-title" className={styles.title}>
            인기 연재 시리즈
            <span className={styles.notice}>무료 회차로 맛보고 한 번 사면 계속 열람</span>
          </h2>
          <p className={styles.subtitle}>시리즈를 사면 이후 올라오는 회차까지 계속 읽을 수 있어요.</p>
        </div>
      </div>
      <ul className={styles.grid}>
        {series.map((item) => (
          <li key={item.id}>
            <SeriesCard series={item} />
          </li>
        ))}
      </ul>
    </section>
  );
}
