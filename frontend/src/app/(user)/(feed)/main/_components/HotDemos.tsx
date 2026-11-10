import Link from "next/link";
import kit from "@/app/_components/ui/kit.module.css";
import {
  ArrowRightIcon,
  ChevronRightIcon,
  HeartIcon,
} from "@/app/_components/ui/Icons";
import type { DemoSummary } from "@/app/_lib/types/demo";
import { DemoThumb } from "../../_components/DemoThumb";
import { VISIBLE_CARDS } from "../_lib/carousel";
import { DemoCarousel } from "./DemoCarousel";
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
        <Link href={`/mini-home/${demo.author.id}/demos/${demo.id}`} className={styles.play}>
          찍먹하기
          <ArrowRightIcon />
        </Link>
      </div>
    </>
  );
}

export function HotDemos({ demos }: Props) {
  // 데모가 하나도 없으면 빈 캐러셀(화살표만 떠 있는 빈 칸) 대신 안내 문구를 보여 준다.
  if (demos.length === 0) {
    return (
      <section aria-labelledby="hot-demos-title">
        <div className={styles.header}>
          <h2 id="hot-demos-title" className={styles.title}>
            지금 뜨는 데모
            <span className={styles.dot} aria-hidden="true" />
          </h2>
        </div>
        <p className={kit.empty}>
          아직 올라온 데모가 없어요. <Link href="/demos/upload">첫 데모를 올려 보세요.</Link>
        </p>
      </section>
    );
  }

  // 마지막 카드 뒤에 같은 카드를 한 번 더 붙여, 화면 폭에 몇 장이 보이든 끝에서 처음으로 끊김 없이 이어지게 한다.
  const loops = demos.length > VISIBLE_CARDS;
  const clones = loops ? demos : [];

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
