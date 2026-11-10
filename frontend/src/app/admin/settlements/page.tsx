import type { Metadata } from "next";
import { Suspense } from "react";
import { connection } from "next/server";
import { ConfirmButton } from "@/app/_components/ui/ConfirmButton";
import kit from "@/app/_components/ui/kit.module.css";
import { formatWon } from "@/app/_lib/format";
import { SETTLEMENT_FEE_PERCENT } from "@/app/_lib/policy";
import { confirmSettlement } from "../_actions";
import { SettlementGenerateForm } from "../_components/SettlementGenerateForm";
import { StatusPill } from "../_components/StatusPill";
import { getSettlements } from "../_lib/api";

/** 서울 기준 "YYYY-MM". offset 0이면 이번 달, -1이면 지난달 */
function seoulMonth(offset: number) {
  const now = new Date(Date.now() + 9 * 60 * 60 * 1000);
  const date = new Date(Date.UTC(now.getUTCFullYear(), now.getUTCMonth() + offset, 1));
  return `${date.getUTCFullYear()}-${String(date.getUTCMonth() + 1).padStart(2, "0")}`;
}

export const metadata: Metadata = { title: "정산 관리" };

async function SettlementsContent() {
  await connection();
  const rows = await getSettlements();
  const pending = rows.filter((row) => row.status === "pending");

  return (
    <>
      <div className={kit.grid3}>
        <div className={`${kit.card} ${kit.stat}`}><span className={kit.statLabel}>정산 대기</span><span className={kit.statValue}>{pending.length}건</span></div>
        <div className={`${kit.card} ${kit.stat}`}><span className={kit.statLabel}>지급 예정액</span><span className={kit.statValue}>{formatWon(pending.reduce((s, r) => s + r.gross - r.fee, 0))}</span></div>
        <div className={`${kit.card} ${kit.stat}`}><span className={kit.statLabel}>수수료율</span><span className={kit.statValue}>{SETTLEMENT_FEE_PERCENT}%</span></div>
      </div>
      <SettlementGenerateForm defaultPeriod={seoulMonth(-1)} maxPeriod={seoulMonth(0)} />
      <section className={kit.card}>
        <div className={kit.tableWrap}>
          <table className={kit.table}>
            <thead>
              <tr>
                <th scope="col">기간</th><th scope="col">작가</th>
                <th scope="col" className={kit.num}>총수익</th><th scope="col" className={kit.num}>수수료</th><th scope="col" className={kit.num}>지급액</th>
                <th scope="col">상태</th><th scope="col"><span className="sr-only">관리</span></th>
              </tr>
            </thead>
            <tbody>
              {rows.map((row) => (
                <tr key={row.id}>
                  <td>{row.period}</td>
                  <td className={kit.strong}>{row.creator}</td>
                  <td className={kit.num}>{formatWon(row.gross)}</td>
                  <td className={kit.num}>-{formatWon(row.fee)}</td>
                  <td className={`${kit.num} ${kit.strong}`}>{formatWon(row.gross - row.fee)}</td>
                  <td><StatusPill status={row.status} /></td>
                  <td>
                    {row.status === "pending" && (
                      <ConfirmButton
                        label="정산 확정"
                        title={`${row.creator}님 ${row.period} 정산을 확정할까요?`}
                        description={`${formatWon(row.gross - row.fee)}이 지급 대기열에 올라가요.`}
                        confirmLabel="확정"
                        className={`${kit.btnPrimary} ${kit.sm}`}
                        onConfirm={confirmSettlement.bind(null, row.id)}
                      />
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        {rows.length === 0 && <p className={kit.empty}>아직 만든 정산이 없어요. 달을 골라 정산을 만들어 보세요.</p>}
      </section>
    </>
  );
}

export default function AdminSettlementsPage() {
  return (
    <>
      <h1 className={kit.pageTitle}>정산 관리</h1>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <SettlementsContent />
      </Suspense>
    </>
  );
}
