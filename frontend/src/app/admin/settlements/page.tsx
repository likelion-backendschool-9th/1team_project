import type { Metadata } from "next";
import { Suspense } from "react";
import { ConfirmButton } from "@/app/_components/ui/ConfirmButton";
import { ComingSoon } from "@/app/_components/ui/ComingSoon";
import kit from "@/app/_components/ui/kit.module.css";
import { isMockMode } from "@/app/_lib/api/client";
import { formatWon } from "@/app/_lib/format";
import { confirmSettlement } from "../_actions";
import { StatusPill } from "../_components/StatusPill";
import { getSettlements } from "../_lib/api";

export const metadata: Metadata = { title: "정산 관리" };

async function SettlementsContent() {
  if (!isMockMode) return <ComingSoon feature="정산 관리" />;

  const rows = await getSettlements();
  const pending = rows.filter((row) => row.status === "pending");

  return (
    <>
      <div className={kit.grid3}>
        <div className={`${kit.card} ${kit.stat}`}><span className={kit.statLabel}>정산 대기</span><span className={kit.statValue}>{pending.length}건</span></div>
        <div className={`${kit.card} ${kit.stat}`}><span className={kit.statLabel}>지급 예정액</span><span className={kit.statValue}>{formatWon(pending.reduce((s, r) => s + r.gross - r.fee, 0))}</span></div>
        <div className={`${kit.card} ${kit.stat}`}><span className={kit.statLabel}>수수료율</span><span className={kit.statValue}>20%</span></div>
      </div>
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
