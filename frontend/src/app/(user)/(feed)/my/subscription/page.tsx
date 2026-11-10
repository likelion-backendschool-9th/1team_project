import type { Metadata } from "next";
import Link from "next/link";
import { Suspense } from "react";
import { Avatar } from "@/app/_components/ui/Avatar";
import { ConfirmButton } from "@/app/_components/ui/ConfirmButton";
import kit from "@/app/_components/ui/kit.module.css";
import { formatDate, formatWon } from "@/app/_lib/format";
import { PLUS_BENEFITS, PLUS_PRICE_WON } from "@/app/_lib/policy";
import { getSeriesPurchases } from "@/app/(user)/blog/_lib/api";
import { cancelPlus, subscribePlus } from "../_actions";
import { getPlus } from "../_lib/api";
import styles from "./Subscription.module.css";

export const metadata: Metadata = { title: "구독·구매 관리", robots: { index: false } };

async function SubscriptionContent() {
  const [plus, purchases] = await Promise.all([getPlus(), getSeriesPurchases()]);
  const spent = purchases.reduce((sum, item) => sum + item.paidPrice, 0);

  return (
    <>
      <section className={styles.plus} aria-labelledby="plus-title">
        <div className={kit.grow}>
          <p className={styles.plusLabel}>DIPSOURCE PLUS</p>
          <h2 id="plus-title" className={styles.plusTitle}>
            딥소스 플러스 {plus.active && <span className={kit.pillSuccess}>이용 중</span>}
          </h2>
          <p className={styles.plusPrice}>
            월 {formatWon(PLUS_PRICE_WON)} · 자동이체
            {plus.active && plus.nextBillingAt && ` · 다음 결제 ${formatDate(plus.nextBillingAt)}`}
          </p>
          <ul className={styles.benefits}>
            {PLUS_BENEFITS.map((benefit) => (
              <li key={benefit}>✓ {benefit}</li>
            ))}
          </ul>
        </div>
        {plus.active ? (
          <ConfirmButton
            label="플러스 해지"
            title="딥소스 플러스를 해지할까요?"
            description="이번 결제 기간이 끝나면 스킨·BGM·고급 배지가 꺼지고, 미니홈피 데모는 3개만 보여요."
            confirmLabel="해지"
            danger
            className={`${kit.btnGhost} ${styles.plusAction}`}
            onConfirm={cancelPlus}
          />
        ) : (
          <ConfirmButton
            label="플러스 시작하기"
            title="딥소스 플러스를 시작할까요?"
            description={`매달 ${formatWon(PLUS_PRICE_WON)}이 자동이체돼요.`}
            confirmLabel="시작하기"
            className={`${kit.btnPrimary} ${styles.plusAction}`}
            onConfirm={subscribePlus}
          />
        )}
      </section>

      <div className={kit.grid3}>
        <div className={`${kit.card} ${kit.stat}`}>
          <span className={kit.statLabel}>구매한 시리즈</span>
          <span className={kit.statValue}>{purchases.length}개</span>
        </div>
        <div className={`${kit.card} ${kit.stat}`}>
          <span className={kit.statLabel}>시리즈 결제 금액</span>
          <span className={kit.statValue}>{formatWon(spent)}</span>
        </div>
        <div className={`${kit.card} ${kit.stat}`}>
          <span className={kit.statLabel}>매달 자동이체</span>
          <span className={kit.statValue}>{plus.nextBillingAt ? formatWon(PLUS_PRICE_WON) : "-"}</span>
        </div>
      </div>
      <section className={kit.card} aria-labelledby="series-purchase-title">
        <h2 id="series-purchase-title" className={kit.cardTitle}>
          구매한 시리즈
        </h2>
        {purchases.length === 0 && (
          <div className={kit.empty}>
            <p>아직 산 시리즈가 없어요.</p>
            <p>
              시리즈는 한 번 사면 이후 올라오는 회차까지 계속 읽을 수 있어요.{" "}
              <Link href="/blog" className={kit.more}>
                시리즈 둘러보기 →
              </Link>
            </p>
          </div>
        )}
        <ul className={kit.list}>
          {purchases.map(({ series, paidPrice, purchasedAt }) => (
            <li key={series.id} className={kit.listItem}>
              <Avatar name={series.author.nickname} size={44} />
              <span className={kit.grow}>
                <Link href={`/blog/series/${series.id}`} className={kit.strong}>
                  {series.title}
                </Link>
                <span className={kit.faint} style={{ display: "block" }}>
                  {series.author.nickname} · {series.status === "completed" ? "완결" : "연재 중"} {series.episodeCount}화 ·{" "}
                  {formatDate(purchasedAt)}에 {formatWon(paidPrice)} 결제
                </span>
              </span>
              <Link href={`/blog/series/${series.id}`} className={`${kit.btnGhost} ${kit.sm}`}>
                회차 보기
              </Link>
            </li>
          ))}
        </ul>
      </section>
    </>
  );
}

export default function SubscriptionPage() {
  return (
    <>
      <h1 className={kit.pageTitle}>구독·구매 관리</h1>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <SubscriptionContent />
      </Suspense>
    </>
  );
}
