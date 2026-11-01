import type { Metadata } from "next";
import { Suspense } from "react";
import kit from "@/app/_components/ui/kit.module.css";
import { formatDate, formatWon } from "@/app/_lib/format";
import { StatusPill } from "../_components/StatusPill";
import { getSubscriptions } from "../_lib/api";

export const metadata: Metadata = { title: "구독 현황" };

async function SubscriptionsContent() {
  const rows = await getSubscriptions();
  const active = rows.filter((row) => row.status === "active");
  const monthly = active.reduce((sum, row) => sum + row.price, 0);
  const plus = active.filter((row) => row.plan === "plus").length;

  return (
    <>
      <div className={kit.grid3}>
        <div className={`${kit.card} ${kit.stat}`}><span className={kit.statLabel}>블로그 구독</span><span className={kit.statValue}>{active.length - plus}건</span></div>
        <div className={`${kit.card} ${kit.stat}`}><span className={kit.statLabel}>딥소스 플러스</span><span className={kit.statValue}>{plus}명</span></div>
        <div className={`${kit.card} ${kit.stat}`}><span className={kit.statLabel}>월 자동이체 합계</span><span className={kit.statValue}>{formatWon(monthly)}</span></div>
      </div>
      <section className={kit.card}>
        <div className={kit.tableWrap}>
          <table className={kit.table}>
            <thead>
              <tr><th scope="col">상품</th><th scope="col">구독자</th><th scope="col">작가</th><th scope="col" className={kit.num}>월 금액</th><th scope="col">시작일</th><th scope="col">상태</th></tr>
            </thead>
            <tbody>
              {rows.map((row) => (
                <tr key={row.id}>
                  <td>
                    <span className={row.plan === "plus" ? kit.pillAccent : kit.pill}>
                      {row.plan === "plus" ? "딥소스 플러스" : "블로그 구독"}
                    </span>
                  </td>
                  <td>{row.subscriber}</td>
                  <td className={kit.strong}>{row.author ?? "-"}</td>
                  <td className={kit.num}>{formatWon(row.price)}</td>
                  <td>{formatDate(row.startedAt)}</td>
                  <td><StatusPill status={row.status} /></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </>
  );
}

export default function AdminSubscriptionsPage() {
  return (
    <>
      <h1 className={kit.pageTitle}>구독 현황</h1>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <SubscriptionsContent />
      </Suspense>
    </>
  );
}
