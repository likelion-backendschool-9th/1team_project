import type { Metadata } from "next";
import { Suspense } from "react";
import { ConfirmButton } from "@/app/_components/ui/ConfirmButton";
import kit from "@/app/_components/ui/kit.module.css";
import { formatWon } from "@/app/_lib/format";
import { confirmSettlement, createSettlements } from "../_actions";
import { StatusPill } from "../_components/StatusPill";
import { getSettlements } from "../_lib/api";

export const metadata: Metadata = { title: "정산 관리" };

async function SettlementsContent() {
  const rows = await getSettlements();
  const pending = rows.filter((row) => row.status === "pending");

  return (
    <>
      <div className={kit.grid3}>
        <div className={`${kit.card} ${kit.stat}`}><span className={kit.statLabel}>정산 대기</span><span className={kit.statValue}>{pending.length}건</span></div>
        <div className={`${kit.card} ${kit.stat}`}><span className={kit.statLabel}>지급 예정액</span><span className={kit.statValue}>{formatWon(pending.reduce((s, r) => s + r.gross - r.fee, 0))}</span></div>
        <div className={`${kit.card} ${kit.stat}`}><span className={kit.statLabel}>수수료율</span><span className={kit.statValue}>30%</span></div>
      </div>
      <div className={kit.row} style={{ justifyContent: "flex-end" }}>
        <ConfirmButton
          label="지난달 정산 만들기"
          title="지난달 정산을 만들까요?"
          description="지난달 판매·후원 수익 칩을 작가별로 모아 정산 대기로 올려요. 이미 만든 작가는 건너뛰어요."
          confirmLabel="만들기"
          className={kit.btnPrimary}
          onConfirm={createSettlements}
        />
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
                        description={`${formatWon(row.gross - row.fee)}이 지급 대기열에 올라가고, 작가의 칩이 그만큼 빠져요.`}
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
