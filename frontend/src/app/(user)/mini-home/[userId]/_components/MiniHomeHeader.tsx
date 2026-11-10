import Link from "next/link";
import { ReportModal } from "@/app/_components/ui/ReportModal";
import { formatCount } from "@/app/_lib/format";
import type { MiniHomeProfile, VisitorStats } from "@/app/_lib/types/miniHome";
import { DmOpenButton } from "@/app/(user)/_components/DmOpenButton";
import { reportUser, surf } from "../_actions";
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
            ✏️ 홈피 설정
          </Link>
        ) : (
          <>
            <IlchonButton
              userId={profile.id}
              nickname={profile.nickname}
              following={profile.following}
            />
            {/* 팔로우하지 않아도 누구에게나 DM을 보낼 수 있다. 화면 이동 없이 DM 창이 열린다. */}
            <DmOpenButton
              user={{
                id: profile.id,
                nickname: profile.nickname,
                profileImage: profile.profileImage,
                techStacks: profile.techStacks,
              }}
              className={styles.button}
            >
              <span aria-hidden="true">💬</span> 메시지
            </DmOpenButton>
            <SponsorButton userId={profile.id} nickname={profile.nickname} chipBalance={chipBalance} />
            <ReportModal
              label="신고"
              title="유저 신고"
              className={styles.button}
              onConfirm={reportUser.bind(null, profile.id)}
            />
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
