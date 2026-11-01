import type { Metadata } from "next";
import Link from "next/link";
import { Suspense } from "react";
import { Avatar } from "@/app/_components/ui/Avatar";
import { ConfirmButton } from "@/app/_components/ui/ConfirmButton";
import kit from "@/app/_components/ui/kit.module.css";
import { formatDate } from "@/app/_lib/format";
import { cancelSubscription } from "../_actions";
import { getSubscriptions } from "../_lib/api";

export const metadata: Metadata = { title: "구독 관리", robots: { index: false } };

async function SubscriptionContent() {
  const subscriptions = await getSubscriptions();
  const active = subscriptions.filter((item) => item.status === "active");
  const monthly = active.reduce((sum, item) => sum + item.chipsPerMonth, 0);

  return (
    <>
      <div className={kit.grid3}>
        <div className={`${kit.card} ${kit.stat}`}>
          <span className={kit.statLabel}>구독 중</span>
          <span className={kit.statValue}>{active.length}개</span>
        </div>
        <div className={`${kit.card} ${kit.stat}`}>
          <span className={kit.statLabel}>매달 나가는 칩</span>
          <span className={kit.statValue}>{monthly}칩</span>
        </div>
        <div className={`${kit.card} ${kit.stat}`}>
          <span className={kit.statLabel}>다음 결제일</span>
          <span className={kit.statValue}>{active[0] ? formatDate(active[0].nextBillingAt) : "-"}</span>
        </div>
      </div>
      <section className={kit.card} aria-labelledby="subscription-title">
        <h2 id="subscription-title" className={kit.cardTitle}>
          구독 중인 연재
        </h2>
        <ul className={kit.list}>
          {subscriptions.map((item) => (
            <li key={item.id} className={kit.listItem}>
              <Avatar name={item.author.nickname} size={44} />
              <span className={kit.grow}>
                <Link href={`/blog/${item.author.id}`} className={kit.strong}>
                  {item.seriesTitle}
                </Link>
                <span className={kit.faint} style={{ display: "block" }}>
                  {item.author.nickname} · 월 {item.chipsPerMonth}칩 ·{" "}
                  {item.status === "active" ? `다음 결제 ${formatDate(item.nextBillingAt)}` : `${formatDate(item.nextBillingAt)}까지 이용 가능`}
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
