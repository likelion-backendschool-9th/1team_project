import type { Metadata } from "next";
import { connection } from "next/server";
import { Suspense } from "react";
import kit from "@/app/_components/ui/kit.module.css";
import { formatWon } from "@/app/_lib/format";
import { getRevenue } from "../_lib/api";
import styles from "../_components/Admin.module.css";

export const metadata: Metadata = { title: "매출 통계" };

const MONTH = /^\d{4}-(0[1-9]|1[0-2])$/;

function parseMonth(value: string | string[] | undefined) {
  const month = Array.isArray(value) ? value[0] : value;
  return month && MONTH.test(month) ? month : undefined;
}

async function RevenueContent({ searchParams }: { searchParams: Promise<Record<string, string | string[] | undefined>> }) {
  await connection();

  const params = await searchParams;
  const from = parseMonth(params.from);
  const to = parseMonth(params.to);
  const rows = await getRevenue(from, to);
  const sum = (row: (typeof rows)[number]) => row.subscription + row.plus + row.chips;
  const total = rows.reduce((acc, row) => acc + sum(row), 0);
  const max = Math.max(1, ...rows.map(sum));
  const share = (key: "subscription" | "plus" | "chips") =>
    total ? Math.round((rows.reduce((acc, row) => acc + row[key], 0) / total) * 100) : 0;

  return (
    <>
      <form action="/admin/revenue" method="get" className={`${kit.card} ${styles.filters}`}>
        <div className={`${kit.field} ${styles.field}`}>
          <label htmlFor="revenue-from" className={kit.label}>시작 월</label>
          <input id="revenue-from" name="from" type="month" defaultValue={from} className={kit.input} />
        </div>
        <div className={`${kit.field} ${styles.field}`}>
          <label htmlFor="revenue-to" className={kit.label}>끝 월</label>
          <input id="revenue-to" name="to" type="month" defaultValue={to} className={kit.input} />
        </div>
        <button type="submit" className={kit.btnDark}>조회</button>
      </form>

      <div className={kit.grid4}>
        <div className={`${kit.card} ${kit.stat}`}><span className={kit.statLabel}>기간 합계</span><span className={kit.statValue}>{formatWon(total)}</span></div>
        <div className={`${kit.card} ${kit.stat}`}><span className={kit.statLabel}>칩 충전 비중</span><span className={kit.statValue}>{share("chips")}%</span></div>
        <div className={`${kit.card} ${kit.stat}`}><span className={kit.statLabel}>블로그 구독 비중</span><span className={kit.statValue}>{share("subscription")}%</span></div>
        <div className={`${kit.card} ${kit.stat}`}><span className={kit.statLabel}>딥소스 플러스 비중</span><span className={kit.statValue}>{share("plus")}%</span></div>
      </div>

      <section className={kit.card} aria-labelledby="revenue-chart">
        <div className={kit.cardHeader}>
          <h2 id="revenue-chart" className={kit.cardTitle}>월별 매출</h2>
          <p className={styles.legend}><span>칩 충전</span><span>구독·플러스 (자동이체)</span></p>
        </div>
        {rows.length === 0 ? (
          <p className={kit.empty}>선택한 기간에 매출이 없어요.</p>
        ) : (
          <div className={styles.chart} role="img" aria-label={rows.map((row) => `${row.month} ${formatWon(sum(row))}`).join(", ")}>
            {rows.map((row) => (
              <div key={row.month} className={styles.bar}>
                <span className={styles.barValue}>{Math.round(sum(row) / 10_000)}만</span>
                <span className={styles.stack} style={{ height: `${(sum(row) / max) * 78}%` }}>
                  <span className={styles.barFill} style={{ flex: row.chips }} />
                  <span className={`${styles.barFill} ${styles.barFillAlt}`} style={{ flex: row.subscription + row.plus }} />
                </span>
                <span className={kit.faint}>{row.month.slice(5)}월</span>
              </div>
            ))}
          </div>
        )}
      </section>

      <section className={kit.card}>
        <div className={kit.tableWrap}>
          <table className={kit.table}>
            <thead>
              <tr>
                <th scope="col">월</th>
                <th scope="col" className={kit.num}>칩 충전</th>
                <th scope="col" className={kit.num}>블로그 구독</th>
                <th scope="col" className={kit.num}>딥소스 플러스</th>
                <th scope="col" className={kit.num}>합계</th>
              </tr>
            </thead>
            <tbody>
              {rows.map((row) => (
                <tr key={row.month}>
                  <td>{row.month}</td>
                  <td className={kit.num}>{formatWon(row.chips)}</td>
                  <td className={kit.num}>{formatWon(row.subscription)}</td>
                  <td className={kit.num}>{formatWon(row.plus)}</td>
                  <td className={`${kit.num} ${kit.strong}`}>{formatWon(sum(row))}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </>
  );
}

export default function AdminRevenuePage({ searchParams }: PageProps<"/admin/revenue">) {
  return (
    <>
      <h1 className={kit.pageTitle}>매출 통계</h1>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <RevenueContent searchParams={searchParams} />
      </Suspense>
    </>
  );
}
