import Link from "next/link";
import { Avatar } from "@/app/_components/ui/Avatar";
import { ArrowRightIcon, HeartIcon } from "@/app/_components/ui/Icons";
import { formatCount } from "@/app/_lib/format";
import type { DemoDetail } from "@/app/_lib/types/demo";
import { DemoThumb } from "../../_components/DemoThumb";
import styles from "./DemoList.module.css";

type Props = {
  demo: DemoDetail;
};

export function DemoCard({ demo }: Props) {
  return (
    <article className={styles.card} aria-labelledby={`demo-card-${demo.id}`}>
      <Link href={`/demos/${demo.id}`} tabIndex={-1} aria-hidden="true">
        <DemoThumb
          seed={demo.id - 1}
          thumbnail={demo.thumbnail}
          label={demo.runtime === "unity" ? "unity webgl" : demo.techTags[0]}
          className={styles.thumb}
        />
      </Link>
      <div className={styles.body}>
        <h2 id={`demo-card-${demo.id}`} className={styles.name}>
          <Link href={`/demos/${demo.id}`}>{demo.title}</Link>
        </h2>
        <p className={styles.desc}>{demo.description}</p>
        <div className={styles.author}>
          <Avatar name={demo.author.nickname} size={24} />
          <span>{demo.author.nickname}</span>
          <span className={styles.stats}>
            <HeartIcon filled />
            <span className="sr-only">좋아요</span>
            {formatCount(demo.likeCount)}
            <span aria-hidden="true">·</span> 실행 {formatCount(demo.playCount)}
          </span>
        </div>
        <Link href={`/demos/${demo.id}`} className={styles.play}>
          찍먹하기
          <ArrowRightIcon />
        </Link>
      </div>
    </article>
  );
}
