import Link from "next/link";
import { formatCount } from "@/app/_lib/format";
import type { MiniHomeProfile, VisitorStats } from "@/app/_lib/types/miniHome";
import { surf } from "../_actions";
import { IlchonButton } from "./IlchonButton";
import { SponsorButton } from "./SponsorButton";
import styles from "./MiniHomeHeader.module.css";

type Props = {
  profile: MiniHomeProfile;
  visitors: VisitorStats;
  isOwner: boolean;
  chipBalance: number;
};

export function MiniHomeHeader({ profile, visitors, isOwner, chipBalance }: Props) {
  return (
    <header className={styles.header}>
      <div className={styles.left}>
        <div className={styles.visit}>
          <p className={styles.counter}>
            <span className={styles.today}>
              TODAY <strong>{formatCount(visitors.today)}</strong>
            </span>
            <span className={styles.divider} aria-hidden="true" />
            <span>
              TOTAL <strong>{formatCount(visitors.total)}</strong>
            </span>
          </p>
          {profile.domain && <span className={styles.domain}>{profile.domain}</span>}
        </div>
        <Link href="/main" className={styles.feedLink}>
          <span aria-hidden="true">←</span> 피드 바로가기
        </Link>
      </div>
      <div className={styles.center}>
        <p className={styles.title}>
          <span aria-hidden="true">✨</span> {profile.nickname}의 홈피
        </p>
        <span className={styles.headline}>{profile.headline}</span>
      </div>
      <div className={styles.right}>
        {isOwner ? (
          <Link href={`/mini-home/${profile.id}/edit`} className={styles.button}>
            ✏️ 홈피 꾸미기
          </Link>
        ) : (
          <>
            <IlchonButton
              userId={profile.id}
              nickname={profile.nickname}
              following={profile.following}
            />
            <SponsorButton userId={profile.id} nickname={profile.nickname} chipBalance={chipBalance} />
          </>
        )}
        <form action={surf.bind(null, profile.id)}>
          <button type="submit" className={styles.button}>
            <span aria-hidden="true">🌊</span> 파도타기
          </button>
        </form>
      </div>
    </header>
  );
}
