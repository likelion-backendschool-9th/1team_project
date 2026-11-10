import type { Metadata } from "next";
import Link from "next/link";
import { connection } from "next/server";
import { redirect } from "next/navigation";
import { Suspense } from "react";
import kit from "@/app/_components/ui/kit.module.css";
import { formatDateTime, formatWon } from "@/app/_lib/format";
import { getMe } from "@/app/(user)/_lib/api";
import { getMyDaily, getMyIncomes, getMySettlements, getMySettlementSummary, getMySources } from "../_lib/api";
import styles from "./Settlements.module.css";

export const metadata: Metadata = { title: "내 정산", robots: { index: false } };

const MONTH = /^\d{4}-\d{2}$/;
const DAY = /^\d{4}-\d{2}-\d{2}$/;

function seoulToday(): string {
  return new Intl.DateTimeFormat("en-CA", { timeZone: "Asia/Seoul" }).format(new Date());
}

function one(value: string | string[] | undefined): string {
  return Array.isArray(value) ? value[0] ?? "" : value ?? "";
}

async function SettlementsContent({
  searchParams,
}: {
  searchParams: Promise<Record<string, string | string[] | undefined>>;
}) {
  await connection();
  if (!(await getMe())) redirect("/login?redirect=/my/settlements");

  const params = await searchParams;
  const today = seoulToday();
  const month = MONTH.test(one(params.month)) ? one(params.month) : today.slice(0, 7);
  const requestedDate = one(params.date);
  const date = DAY.test(requestedDate) && requestedDate.startsWith(month) ? requestedDate : "";
  const page = Math.max(1, Number(one(params.page)) || 1);

  const [summary, settlements, daily, sources, incomes] = await Promise.all([
    getMySettlementSummary(),
    getMySettlements(page),
    getMyDaily(month),
    getMySources(month),
    getMyIncomes(date ? { date } : { month }),
  ]);
  const maxDaily = Math.max(0, ...daily.map((row) => row.gross));
  const maxSource = Math.max(0, ...sources.map((row) => row.gross));
  const query = (next: { date?: string; page?: number }) => {
    const search = new URLSearchParams({ month });
    if (next.date) search.set("date", next.date);
    if (next.page && next.page > 1) search.set("page", String(next.page));
    return `/my/settlements?${search}`;
  };

  return (
    <>
      <ul className={styles.cards}>
        <li className={kit.card}>
          <p className={kit.statLabel}>이번 달 예상 수익</p>
          <p className={kit.statValue}>{formatWon(summary.expectedGross)}</p>
        </li>
        <li className={kit.card}>
          <p className={kit.statLabel}>예상 지급액</p>
          <p className={kit.statValue}>{formatWon(summary.expectedNet)}</p>
          <p className={kit.muted}>수수료 {formatWon(summary.expectedFee)} · {summary.feePercent}%</p>
        </li>
        <li className={kit.card}>
          <p className={kit.statLabel}>누적 지급액</p>
          <p className={kit.statValue}>{formatWon(summary.paidTotal)}</p>
        </li>
      </ul>

      <section className={`${kit.card} ${styles.section}`} aria-labelledby="source-title">
        <h2 id="source-title" className={kit.cardTitle}>수익 출처</h2>
        <div className={styles.sources}>
          {sources.map((source) => {
            const height = maxSource === 0 ? 2 : Math.max(2, Math.round((source.gross / maxSource) * 100));
            return (
              <div key={source.label} className={styles.source}>
                <span className={styles.track}>
                  <span className={styles.column} style={{ height: `${height}%` }} />
                </span>
                <b>{source.label}</b>
                <span className={kit.muted}>{formatWon(source.gross)} · {source.count}건</span>
              </div>
            );
          })}
        </div>
      </section>

      <section className={`${kit.card} ${styles.section}`} aria-labelledby="daily-title">
        <div className={styles.month}>
          <h2 id="daily-title" className={kit.cardTitle}>일별 수익</h2>
          <form action="/my/settlements" className={styles.month}>
            <label htmlFor="settlement-month">월</label>
            <input id="settlement-month" name="month" type="month" defaultValue={month} />
            <button type="submit" className={`${kit.btn} ${kit.sm}`}>보기</button>
          </form>
        </div>
        <div className={styles.chart} role="list" aria-label={`${month} 일별 수익`}>
          {daily.map((row) => {
            const height = maxDaily === 0 ? 2 : Math.max(row.gross > 0 ? 8 : 2, Math.round((row.gross / maxDaily) * 100));
            const selected = date === row.date;
            return (
              <Link
                key={row.date}
                href={query(selected ? {} : { date: row.date })}
                className={styles.bar}
                role="listitem"
                aria-current={selected ? "date" : undefined}
                aria-label={`${row.date} ${formatWon(row.gross)} ${selected ? "선택 해제" : ""}`.trim()}
              >
                <span className={styles.track}>
                  <span className={styles.column} style={{ height: `${height}%` }} />
                </span>
                <span className={styles.day}>{Number(row.date.slice(8))}</span>
              </Link>
            );
          })}
        </div>
      </section>

      <section className={kit.card} aria-labelledby="income-title">
        <h2 id="income-title" className={kit.cardTitle}>
          수익 내역{date ? ` · ${date}` : ` · ${month}`}
        </h2>
        {incomes.length === 0 ? (
          <p className={kit.empty}>이 기간에 수익이 없어요.</p>
        ) : (
          <div className={kit.tableWrap}>
            <table className={kit.table}>
              <thead>
                <tr>
                  <th>일시</th>
                  <th>종류</th>
                  <th>대상</th>
                  <th>보낸 사람</th>
                  <th className={kit.num}>칩</th>
                  <th className={kit.num}>원화</th>
                </tr>
              </thead>
              <tbody>
                {incomes.map((row, index) => (
                  <tr key={`${row.occurredAt}-${row.targetId ?? index}`}>
                    <td>{formatDateTime(row.occurredAt)}</td>
                    <td>{row.label}</td>
                    <td>{row.title ?? "삭제된 글"}{row.targetId ? ` #${row.targetId}` : ""}</td>
                    <td>{row.senderNickname ?? "알 수 없음"}</td>
                    <td className={kit.num}>{row.chips}</td>
                    <td className={kit.num}>{formatWon(row.gross)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <section className={kit.card} aria-labelledby="settlement-title">
        <h2 id="settlement-title" className={kit.cardTitle}>월별 정산</h2>
        {settlements.items.length === 0 ? (
          <p className={kit.empty}>아직 정산 내역이 없어요. 유료 글·시리즈가 판매되면 다음 달에 정산돼요.</p>
        ) : (
          <div className={kit.tableWrap}>
            <table className={kit.table}>
              <thead>
                <tr>
                  <th>월</th>
                  <th className={kit.num}>수익</th>
                  <th className={kit.num}>수수료</th>
                  <th className={kit.num}>지급액</th>
                  <th>상태</th>
                </tr>
              </thead>
              <tbody>
                {settlements.items.map((row) => (
                  <tr key={row.id}>
                    <td>{row.period}</td>
                    <td className={kit.num}>{formatWon(row.gross)}</td>
                    <td className={kit.num}>{formatWon(row.fee)}</td>
                    <td className={kit.num}>{formatWon(row.netAmount)}</td>
                    <td>
                      <span className={row.status === "CONFIRMED" ? kit.pillSuccess : kit.pillWarn}>
                        {row.status === "CONFIRMED" ? "확정" : "대기"}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        {settlements.totalPages > 1 && (
          <p className={styles.month}>
            {page > 1 && <Link href={query({ date, page: page - 1 })}>이전</Link>}
            <span>{page} / {settlements.totalPages}</span>
            {page < settlements.totalPages && <Link href={query({ date, page: page + 1 })}>다음</Link>}
          </p>
        )}
      </section>
    </>
  );
}

export default function SettlementsPage({
  searchParams,
}: {
  searchParams: Promise<Record<string, string | string[] | undefined>>;
}) {
  return (
    <>
      <h1 className={kit.pageTitle}>내 정산</h1>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <SettlementsContent searchParams={searchParams} />
      </Suspense>
    </>
  );
}
