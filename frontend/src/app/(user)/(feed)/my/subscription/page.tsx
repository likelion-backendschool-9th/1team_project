import type { Metadata } from "next";
import Link from "next/link";
import { Suspense } from "react";
import { Avatar } from "@/app/_components/ui/Avatar";
import { ConfirmButton } from "@/app/_components/ui/ConfirmButton";
import kit from "@/app/_components/ui/kit.module.css";
import { formatDate, formatWon } from "@/app/_lib/format";
import { PLUS_BENEFITS, PLUS_PRICE_WON } from "@/app/_lib/policy";
import { cancelPlus, cancelSubscription, subscribePlus } from "../_actions";
import { getPlus, getSubscriptions } from "../_lib/api";
import styles from "./Subscription.module.css";

export const metadata: Metadata = { title: "구독 관리", robots: { index: false } };

async function SubscriptionContent() {
  const [plus, subscriptions] = await Promise.all([getPlus(), getSubscriptions()]);
  const active = subscriptions.filter((item) => item.status === "active");
  const monthly = active.reduce((sum, item) => sum + item.pricePerMonth, 0) + (plus.active ? PLUS_PRICE_WON : 0);

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
          <span className={kit.statLabel}>구독 중인 블로그</span>
          <span className={kit.statValue}>{active.length}개</span>
        </div>
        <div className={`${kit.card} ${kit.stat}`}>
          <span className={kit.statLabel}>매달 자동이체 금액</span>
          <span className={kit.statValue}>{formatWon(monthly)}</span>
        </div>
        <div className={`${kit.card} ${kit.stat}`}>
          <span className={kit.statLabel}>다음 결제일</span>
          <span className={kit.statValue}>{active[0] ? formatDate(active[0].nextBillingAt) : "-"}</span>
        </div>
      </div>
      <section className={kit.card} aria-labelledby="subscription-title">
        <h2 id="subscription-title" className={kit.cardTitle}>
          블로그 구독
        </h2>
        {subscriptions.length === 0 && (
          <div className={kit.empty}>
            <p>아직 구독한 블로그가 없어요.</p>
            <p>
              블로그 주인 화면의 <strong>구독하기</strong>로 구독하면 여기에 보여요.{" "}
              <Link href="/blog" className={kit.more}>
                블로그 둘러보기 →
              </Link>
            </p>
          </div>
        )}
        <ul className={kit.list}>
          {subscriptions.map((item) => (
            <li key={item.id} className={kit.listItem}>
              <Avatar name={item.author.nickname} size={44} />
              <span className={kit.grow}>
                <Link href={`/blog/${item.author.id}`} className={kit.strong}>
                  {item.blogTitle}
                </Link>
                <span className={kit.faint} style={{ display: "block" }}>
                  월 {formatWon(item.pricePerMonth)} 자동이체 ·{" "}
                  {item.status === "active"
                    ? `다음 결제 ${formatDate(item.nextBillingAt)}`
                    : `${formatDate(item.nextBillingAt)}까지 이용 가능`}
                </span>
              </span>
              {item.status === "active" ? (
                <ConfirmButton
                  label="해지"
                  title="구독을 해지할까요?"
                  description="이번 결제 기간이 끝날 때까지는 계속 볼 수 있어요."
                  confirmLabel="해지"
                  danger
                  className={`${kit.btnGhost} ${kit.sm}`}
                  onConfirm={cancelSubscription.bind(null, item.id)}
                />
              ) : (
                <span className={kit.pill}>해지됨</span>
              )}
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
      <h1 className={kit.pageTitle}>구독 관리</h1>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <SubscriptionContent />
      </Suspense>
    </>
  );
}
