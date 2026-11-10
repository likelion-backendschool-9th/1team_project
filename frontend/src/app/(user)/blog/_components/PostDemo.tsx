import Link from "next/link";
import { ArrowRightIcon } from "@/app/_components/ui/Icons";
import type { PostSummary } from "@/app/_lib/types/post";
import { DemoThumb } from "@/app/(user)/(feed)/_components/DemoThumb";
import styles from "./PostDemo.module.css";

type Props = {
  demo: NonNullable<PostSummary["demo"]>;
};

/** 블로그 글에 연결한 데모. 피드 카드의 데모처럼 썸네일·기술 태그와 찍먹하기 버튼을 보여 준다 */
export function PostDemo({ demo }: Props) {
  return (
    <div className={styles.demo}>
      <DemoThumb seed={demo.id - 1} thumbnail={demo.thumbnail ?? null} className={styles.thumb} />
      <div className={styles.info}>
        <p className={styles.title}>{demo.title}</p>
        {demo.description && <p className={styles.desc}>{demo.description}</p>}
        {demo.techTags && demo.techTags.length > 0 && (
          <ul className={styles.tags} aria-label="기술 태그">
            {demo.techTags.map((tag) => (
              <li key={tag}>{tag}</li>
            ))}
          </ul>
        )}
      </div>
      <Link href={`/demos/${demo.id}`} className={styles.play}>
        찍먹하기
        <ArrowRightIcon />
      </Link>
    </div>
  );
}
