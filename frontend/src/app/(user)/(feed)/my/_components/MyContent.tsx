import Link from "next/link";
import { Avatar } from "@/app/_components/ui/Avatar";
import { ConfirmButton } from "@/app/_components/ui/ConfirmButton";
import kit from "@/app/_components/ui/kit.module.css";
import { formatCount, formatDate } from "@/app/_lib/format";
import { UserBadges } from "@/app/(user)/_components/UserBadges";
import { getMe } from "@/app/(user)/_lib/api";
import { logout, verifyCompany, withdraw } from "../_actions";
import { getAccount } from "../_lib/api";
import { AccountForm } from "./AccountForm";
import styles from "./My.module.css";

const providers = { github: "GitHub", kakao: "카카오", google: "구글" };

export async function MyContent() {
  const [me, account] = await Promise.all([getMe(), getAccount()]);

  return (
    <div className={kit.stack}>
      <section className={`${kit.card} ${styles.summary}`} aria-label="프로필 요약">
        <Avatar name={me.nickname} size={72} />
        <div className={kit.grow}>
          <p className={styles.name}>
            {me.nickname}
            {me.plus && (
              <Link href="/my/subscription" className={styles.plusTag}>
                PLUS
              </Link>
            )}
            {me.company && <span className={kit.pill}>🏢 {me.company}</span>}
          </p>
          <UserBadges badges={me.badges} />
          <p className={kit.faint} style={{ marginTop: 8 }}>
            {providers[account.provider]} 계정 · {account.email} · {formatDate(account.joinedAt)} 가입
          </p>
        </div>
        <dl className={styles.stats}>
          <div>
            <dt>팔로워</dt>
            <dd>{formatCount(me.followerCount)}</dd>
          </div>
          <div>
            <dt>팔로잉</dt>
            <dd>{formatCount(me.followingCount)}</dd>
          </div>
          <div>
            <dt>보유 칩</dt>
            <dd className={styles.chip}>{formatCount(me.chipBalance)}</dd>
          </div>
        </dl>
      </section>

      <div className={kit.grid4}>
        <Link href={`/mini-home/${me.id}`} className={styles.shortcut}>
          <span aria-hidden="true">🏠</span>내 미니홈피
        </Link>
        <Link href={`/blog/${me.id}`} className={styles.shortcut}>
          <span aria-hidden="true">📝</span>내 블로그
        </Link>
        <Link href="/demos/upload" className={styles.shortcut}>
          <span aria-hidden="true">📦</span>데모 올리기
        </Link>
        <Link href="/my/coins" className={styles.shortcut}>
          <span aria-hidden="true">🪙</span>칩 충전
        </Link>
      </div>

      <AccountForm nickname={me.nickname} techStacks={me.techStacks.join(", ")} account={account} />

      <section className={kit.card} aria-labelledby="company-title">
        <div className={kit.between}>
          <div>
            <h2 id="company-title" className={kit.cardTitle}>
              회사 인증
            </h2>
            <p className={kit.faint}>
              {me.company
                ? `${me.company} 인증 완료 · 프로필에 회사 태그가 표시돼요.`
                : "회사 이메일로 인증하면 프로필에 회사 태그를 달 수 있어요."}
            </p>
          </div>
          {!me.company && (
            <ConfirmButton
              label="회사 인증하기"
              title="회사 인증을 시작할까요?"
              description="회사 이메일로 인증 메일을 보내요."
              confirmLabel="인증 시작"
              className={kit.btnDark}
              onConfirm={verifyCompany}
            />
          )}
        </div>
      </section>

      <section className={kit.card} aria-labelledby="account-danger">
        <h2 id="account-danger" className={kit.cardTitle}>
          계정
        </h2>
        <div className={kit.row} style={{ marginTop: 14 }}>
          <ConfirmButton
            label="로그아웃"
            title="로그아웃할까요?"
            confirmLabel="로그아웃"
            onConfirm={logout}
          />
          <ConfirmButton
            label="회원 탈퇴"
            title="정말 탈퇴할까요?"
            description="작성한 데모·글·방명록과 보유 칩이 모두 삭제되고 되돌릴 수 없어요."
            confirmLabel="탈퇴"
            danger
            className={kit.btnDanger}
            onConfirm={withdraw}
          />
        </div>
      </section>
    </div>
  );
}
