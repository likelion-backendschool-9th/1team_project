import Link from "next/link";
import { Avatar } from "@/app/_components/ui/Avatar";
import { ChevronRightIcon } from "@/app/_components/ui/Icons";
import type { PostSeries } from "@/app/_lib/types/post";
import styles from "./PopularSeries.module.css";

type Props = {
  series: PostSeries[];
};

export function PopularSeries({ series }: Props) {
  // 연재 데이터가 없으면(백엔드 미구현) 섹션을 통째로 숨긴다.
  if (series.length === 0) return null;
  return (
    <section aria-labelledby="series-title">
      <div className={styles.header}>
        <div>
          <h2 id="series-title" className={styles.title}>
            인기 연재 시리즈
            <span className={styles.notice}>1~3편 무료 맛보기 후 칩 열람</span>
          </h2>
          <p className={styles.subtitle}>
            네이버 프리미엄 모델 기반으로 검증된 시니어 개발자들의 실무 아키텍처 연재
          </p>
        </div>
        <Link href="/blog?sort=popular" className={styles.more}>
          전체 연재
          <ChevronRightIcon />
        </Link>
      </div>
      <ul className={styles.grid}>
        {series.map((item) => (
          <li key={item.id}>
            <article className={styles.card} aria-labelledby={`series-${item.id}`}>
              <div className={styles.top}>
                <span className={styles.status}>
                  {item.status === "completed"
                    ? `총 ${item.episodeCount}화 완결`
                    : `${item.schedule} 연재중`}
                </span>
                <span className={styles.free}>1~{item.freeEpisodes}회 무료 공개</span>
              </div>
              <h3 id={`series-${item.id}`} className={styles.name}>
                {item.title}
              </h3>
              <p className={styles.desc}>{item.description}</p>
              <ul className={styles.tags} aria-label="기술 태그">
                {item.tags.map((tag) => (
                  <li key={tag}>{tag}</li>
                ))}
              </ul>
              <div className={styles.footer}>
                <Link href={`/blog/${item.author.id}`} className={styles.author}>
                  <Avatar name={item.author.nickname} size={22} />
                  <span>
                    {item.author.nickname} ({item.authorTitle})
                  </span>
                </Link>
                <Link href={`/blog/posts/${item.firstPostId}`} className={styles.preview}>
                  맛보기 읽기
                </Link>
                <Link href={`/blog/${item.author.id}`} className={styles.open}>
                  연재 열람 ({item.price} 칩)
                </Link>
              </div>
            </article>
          </li>
        ))}
      </ul>
    </section>
  );
}
