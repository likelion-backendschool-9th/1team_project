import Link from "next/link";
import {
  ArrowRightIcon,
  ChevronRightIcon,
  HeartIcon,
} from "@/app/_components/ui/Icons";
import type { DemoSummary } from "@/app/_lib/types/demo";
import { DemoThumb } from "../../_components/DemoThumb";
import { DemoCarousel, VISIBLE_CARDS } from "./DemoCarousel";
import styles from "./HotDemos.module.css";

type Props = {
  demos: DemoSummary[];
};

function DemoCard({ demo }: { demo: DemoSummary }) {
  return (
    <>
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
    </>
  );
}

export function HotDemos({ demos }: Props) {
  // 마지막 카드 뒤에 앞쪽 카드를 한 번 더 붙여, 끝에서 처음으로 끊김 없이 이어지게 한다.
  const loops = demos.length > VISIBLE_CARDS;
  const clones = loops ? demos.slice(0, VISIBLE_CARDS) : [];

  return (
    <section aria-labelledby="hot-demos-title">
      <DemoCarousel
        count={demos.length}
        autoplay={loops}
        title={
          <h2 id="hot-demos-title" className={styles.title}>
            지금 뜨는 데모
            <span className={styles.dot} aria-hidden="true" />
          </h2>
        }
        more={
          <Link href="/demos?sort=trending" className={styles.more}>
            전체 보기
            <ChevronRightIcon />
          </Link>
        }
      >
        {demos.map((demo) => (
          <li key={demo.id} className={styles.card}>
            <DemoCard demo={demo} />
          </li>
        ))}
        {clones.map((demo) => (
          <li key={`clone-${demo.id}`} className={styles.card} aria-hidden="true" inert>
            <DemoCard demo={demo} />
          </li>
        ))}
      </DemoCarousel>
    </section>
  );
}
