import Link from "next/link";
import { ArrowRightIcon, CommentIcon, HeartIcon } from "@/app/_components/ui/Icons";
import { formatCount } from "@/app/_lib/format";
import type { MiniHomeDemo } from "@/app/_lib/types/miniHome";
import { DemoArt } from "./DemoArt";
import { LaterButton } from "./LaterButton";
import { ParticleCanvas } from "./ParticleCanvas";
import styles from "./FeaturedDemo.module.css";

type Props = {
  demo: MiniHomeDemo | undefined;
  variant: number;
  postId: number | null;
};

export function FeaturedDemo({ demo, variant, postId }: Props) {
  return (
    <section className={styles.card} aria-labelledby="featured-title">
      <div className={styles.header}>
        <h2 id="featured-title" className={styles.title}>
          <span className={styles.live} aria-hidden="true" />
          대표 데모 (LIVE SANDBOX)
        </h2>
        <span className={styles.hint}>브라우저 내 직접 인터랙션 지원</span>
      </div>

      {demo ? (
        <div className={styles.body}>
          <div className={styles.stage}>
            {variant % 3 === 0 ? (
              <ParticleCanvas className={styles.canvas} />
            ) : (
              <DemoArt variant={variant} caption={demo.engine} className={styles.canvas} />
            )}
            <span className={styles.badge}>
              <span className={styles.badgeDot} aria-hidden="true" />
              {demo.title}
            </span>
            {variant % 3 === 0 && (
              <span className={styles.tip}>마우스를 움직여 파티클을 조작하세요</span>
            )}
          </div>

          <div className={styles.info}>
            <h3 className={styles.name}>{demo.title}</h3>
            <p className={styles.engine}>{demo.engine}</p>
            <p className={styles.stats}>
              <span className={styles.likes}>
                <HeartIcon filled />
                <span className="sr-only">좋아요</span>
                {formatCount(demo.likeCount)}
              </span>
              <span>
                <span aria-hidden="true">▸</span> 체험 {formatCount(demo.playCount)}
              </span>
              <span>
                <CommentIcon />
                <span className="sr-only">댓글</span>
                {formatCount(demo.commentCount)}
              </span>
            </p>
            <p className={styles.desc}>{demo.description}</p>

            <Link
              href={`/demos/${demo.id}`}
              target="_blank"
              rel="noopener"
              className={styles.run}
            >
              <span aria-hidden="true">▸</span> 새 창에서 전체화면 실행
              <span className="sr-only">(새 창)</span>
            </Link>
            <div className={styles.actions}>
              <LaterButton title={demo.title} />
              {postId ? (
                <Link href={`/blog/posts/${postId}`} className={styles.making}>
                  제작기 글
                  <ArrowRightIcon />
                </Link>
              ) : (
                <span className={`${styles.making} ${styles.disabled}`} aria-disabled="true">
                  제작기 글 없음
                </span>
              )}
            </div>
            <p className={styles.rule}>업로드 규격: zip · html/css/js 단일 구동 지원</p>
          </div>
        </div>
      ) : (
        <p className={styles.empty}>아직 공개된 데모가 없어요.</p>
      )}
    </section>
  );
}
