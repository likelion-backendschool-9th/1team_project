import type { Metadata } from "next";
import { Suspense } from "react";
import kit from "@/app/_components/ui/kit.module.css";
import { formatWon } from "@/app/_lib/format";
import { getRevenue } from "../_lib/api";
import styles from "../_components/Admin.module.css";

export const metadata: Metadata = { title: "매출 통계" };

async function RevenueContent() {
  const rows = await getRevenue();
  const max = Math.max(...rows.map((row) => row.subscription + row.chips));
  const total = rows.reduce((sum, row) => sum + row.subscription + row.chips, 0);

  return (
    <>
      <div className={kit.grid3}>
        <div className={`${kit.card} ${kit.stat}`}><span className={kit.statLabel}>6개월 누적</span><span className={kit.statValue}>{formatWon(total)}</span></div>
        <div className={`${kit.card} ${kit.stat}`}><span className={kit.statLabel}>구독 비중</span><span className={kit.statValue}>{Math.round((rows.reduce((s, r) => s + r.subscription, 0) / total) * 100)}%</span></div>
        <div className={`${kit.card} ${kit.stat}`}><span className={kit.statLabel}>이번 달 (진행 중)</span><span className={kit.statValue}>{formatWon(rows.at(-1)!.subscription + rows.at(-1)!.chips)}</span></div>
      </div>
      <section className={kit.card} aria-labelledby="revenue-chart">
        <div className={kit.cardHeader}>
          <h2 id="revenue-chart" className={kit.cardTitle}>월별 매출</h2>
          <p className={styles.legend}><span>칩 충전</span><span>구독</span></p>
        </div>
        <div className={styles.chart} role="img" aria-label={rows.map((row) => `${row.month} ${formatWon(row.subscription + row.chips)}`).join(", ")}>
          {rows.map((row) => (
            <div key={row.month} className={styles.bar}>
              <span className={styles.barValue}>{Math.round((row.subscription + row.chips) / 10_000)}만</span>
              <span className={styles.barFill} style={{ height: `${(row.chips / max) * 75}%`, borderRadius: "8px 8px 0 0" }} />
              <span className={`${styles.barFill} ${styles.barFillAlt}`} style={{ height: `${(row.subscription / max) * 75}%`, borderRadius: 0, marginTop: -6 }} />
              <span className={kit.faint}>{row.month.slice(5)}월</span>
            </div>
          ))}
        </div>
      </section>
      <section className={kit.card}>
        <div className={kit.tableWrap}>
          <table className={kit.table}>
            <thead><tr><th scope="col">월</th><th scope="col" className={kit.num}>칩 충전</th><th scope="col" className={kit.num}>구독</th><th scope="col" className={kit.num}>합계</th></tr></thead>
            <tbody>
              {rows.map((row) => (
                <tr key={row.month}>
                  <td>{row.month}</td>
                  <td className={kit.num}>{formatWon(row.chips)}</td>
                  <td className={kit.num}>{formatWon(row.subscription)}</td>
                  <td className={`${kit.num} ${kit.strong}`}>{formatWon(row.chips + row.subscription)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </>
  );
}

export default function AdminRevenuePage() {
  return (
    <>
      <h1 className={kit.pageTitle}>매출 통계</h1>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <RevenueContent />
      </Suspense>
    </>
  );
}
