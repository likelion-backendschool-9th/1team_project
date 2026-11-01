import Link from "next/link";
import {
  ArrowRightIcon,
  ChevronRightIcon,
  HeartIcon,
} from "@/app/_components/ui/Icons";
import type { DemoSummary } from "@/app/_lib/types/demo";
import { DemoThumb } from "./DemoThumb";
import styles from "./HotDemos.module.css";

type Props = {
  demos: DemoSummary[];
};

export function HotDemos({ demos }: Props) {
  return (
    <section aria-labelledby="hot-demos-title">
      <div className={styles.header}>
        <h2 id="hot-demos-title" className={styles.title}>
          지금 뜨는 데모
          <span className={styles.dot} aria-hidden="true" />
        </h2>
        <Link href="/demos?sort=trending" className={styles.more}>
          전체 보기
          <ChevronRightIcon />
        </Link>
      </div>
      <ul className={styles.grid}>
        {demos.map((demo) => (
          <li key={demo.id} className={styles.card}>
            <DemoThumb
              seed={demo.id - 1}
              thumbnail={demo.thumbnail}
              label={demo.techTags[0]}
              className={styles.thumb}
            />
            <div className={styles.body}>
              <h3 className={styles.name}>{demo.title}</h3>
              <p className={styles.meta}>
                <span>@{demo.author.nickname}</span>
                <span className={styles.likes}>
                  <HeartIcon filled />
                  <span className="sr-only">좋아요</span>
                  {demo.likeCount}
                </span>
              </p>
              <Link href={`/demos/${demo.id}`} className={styles.play}>
                찍먹하기
                <ArrowRightIcon />
              </Link>
            </div>
          </li>
        ))}
      </ul>
    </section>
  );
}
