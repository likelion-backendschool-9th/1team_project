import Link from "next/link";
import { Avatar } from "@/app/_components/ui/Avatar";
import { formatWon } from "@/app/_lib/format";
import type { PostSeries } from "@/app/_lib/types/post";
import styles from "./PopularSeries.module.css";

type Props = {
  series: PostSeries;
};

/** 시리즈 카드. 블로그 홈 인기 시리즈와 작가 블로그의 시리즈 목록에서 쓴다 */
export function SeriesCard({ series }: Props) {
  const status =
    series.status === "completed"
      ? `총 ${series.episodeCount}화 완결`
      : `${series.schedule ? `${series.schedule} ` : ""}연재 중 · ${series.episodeCount}화`;
  return (
    <article className={styles.card} aria-labelledby={`series-${series.id}`}>
      <div className={styles.top}>
        <span className={styles.status}>{status}</span>
        {series.price > 0 && series.freeEpisodes > 0 && (
          <span className={styles.free}>무료 {series.freeEpisodes}화</span>
        )}
      </div>
      <h3 id={`series-${series.id}`} className={styles.name}>
        <Link href={`/blog/series/${series.id}`}>{series.title}</Link>
      </h3>
      {series.description && <p className={styles.desc}>{series.description}</p>}
      {series.tags.length > 0 && (
        <ul className={styles.tags} aria-label="기술 태그">
          {series.tags.map((tag) => (
            <li key={tag}>{tag}</li>
          ))}
        </ul>
      )}
      <div className={styles.footer}>
        <Link href={`/blog/${series.author.id}`} className={styles.author}>
          <Avatar name={series.author.nickname} size={22} />
          <span>{series.author.nickname}</span>
        </Link>
        {series.firstPostId && (
          <Link href={`/blog/posts/${series.firstPostId}`} className={styles.preview}>
            1화 읽기
          </Link>
        )}
        <Link href={`/blog/series/${series.id}`} className={styles.open}>
          {series.price > 0 ? `시리즈 보기 · ${formatWon(series.price)}` : "무료 시리즈 보기"}
        </Link>
      </div>
    </article>
  );
}
