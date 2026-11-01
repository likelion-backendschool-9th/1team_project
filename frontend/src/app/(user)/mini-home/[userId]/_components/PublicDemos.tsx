import Link from "next/link";
import { ArrowRightIcon } from "@/app/_components/ui/Icons";
import { formatCount } from "@/app/_lib/format";
import type { MiniHomeDemo } from "@/app/_lib/types/miniHome";
import { DemoArt } from "./DemoArt";
import styles from "./PublicDemos.module.css";

type Props = {
  userId: number;
  demos: MiniHomeDemo[];
  featuredId: number | undefined;
  showMore?: boolean;
  title?: string;
  showcaseIds?: number[];
};

export function PublicDemos({
  userId,
  demos,
  featuredId,
  showMore = true,
  title = "공개된 데모",
  showcaseIds,
}: Props) {
  return (
    <section aria-labelledby="public-demos-title">
      <div className={styles.header}>
        <h2 id="public-demos-title" className={styles.title}>
          <span aria-hidden="true">📁</span> {title}
        </h2>
        {showMore && (
          <Link href={`/mini-home/${userId}/demos`} className={styles.more}>
            전체보기
            <ArrowRightIcon />
          </Link>
        )}
      </div>
      {demos.length === 0 ? (
        <p className={styles.empty}>아직 공개된 데모가 없어요.</p>
      ) : (
        <ul className={styles.grid}>
          {demos.map((demo, index) => (
            <li key={demo.id}>
              <article className={styles.card} aria-labelledby={`demo-${demo.id}`}>
                <div className={styles.thumbWrap}>
                  <DemoArt variant={index} caption={demo.engine} className={styles.thumb} />
                  {demo.id === featuredId ? (
                    <span className={styles.featured}>대표</span>
                  ) : (
                    showcaseIds?.includes(demo.id) && <span className={styles.featured}>공개 중</span>
                  )}
                </div>
                <h3 id={`demo-${demo.id}`} className={styles.name}>
                  {demo.title}
                </h3>
                <p className={styles.desc}>{demo.description}</p>
                <div className={styles.footer}>
                  <p className={styles.meta}>
                    <span aria-hidden="true">♥</span>
                    <span className="sr-only">좋아요</span> {formatCount(demo.likeCount)} · 실행{" "}
                    {formatCount(demo.playCount)}
                  </p>
                  <Link href={`/demos/${demo.id}`} className={styles.play}>
                    찍먹하기
                    <ArrowRightIcon />
                  </Link>
                </div>
              </article>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
