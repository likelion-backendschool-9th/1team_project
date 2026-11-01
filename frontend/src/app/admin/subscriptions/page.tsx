import type { Metadata } from "next";
import { Suspense } from "react";
import kit from "@/app/_components/ui/kit.module.css";
import { formatDate } from "@/app/_lib/format";
import { StatusPill } from "../_components/StatusPill";
import { getSubscriptions } from "../_lib/api";

export const metadata: Metadata = { title: "구독 현황" };

async function SubscriptionsContent() {
  const rows = await getSubscriptions();
  const active = rows.filter((row) => row.status === "active");
  const chips = active.reduce((sum, row) => sum + row.chips, 0);
  const authors = new Set(active.map((row) => row.author)).size;

  return (
    <>
      <div className={kit.grid3}>
        <div className={`${kit.card} ${kit.stat}`}><span className={kit.statLabel}>활성 구독</span><span className={kit.statValue}>{active.length}건</span></div>
        <div className={`${kit.card} ${kit.stat}`}><span className={kit.statLabel}>구독받는 작가</span><span className={kit.statValue}>{authors}명</span></div>
        <div className={`${kit.card} ${kit.stat}`}><span className={kit.statLabel}>월 구독 칩</span><span className={kit.statValue}>{chips}칩</span></div>
      </div>
      <section className={kit.card}>
        <div className={kit.tableWrap}>
          <table className={kit.table}>
            <thead>
              <tr><th scope="col">구독자</th><th scope="col">작가</th><th scope="col" className={kit.num}>월 칩</th><th scope="col">시작일</th><th scope="col">상태</th></tr>
            </thead>
            <tbody>
              {rows.map((row) => (
                <tr key={row.id}>
                  <td>{row.subscriber}</td>
                  <td className={kit.strong}>{row.author}</td>
                  <td className={kit.num}>{row.chips}</td>
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
