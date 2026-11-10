import type { Metadata } from "next";
import { Suspense } from "react";
import { ConfirmButton } from "@/app/_components/ui/ConfirmButton";
import kit from "@/app/_components/ui/kit.module.css";
import { formatCount, formatDate, formatWon } from "@/app/_lib/format";
import { CHIP_PRICE_WON, chipsToWon } from "@/app/_lib/policy";
import { approveRefund } from "../_actions";
import { StatusPill } from "../_components/StatusPill";
import { getCoinTx } from "../_lib/api";

export const metadata: Metadata = { title: "충전·환불" };

async function CoinsContent() {
  const rows = await getCoinTx();
  // 회원 환불 요청 기능은 없어서, 관리자가 충전 건에서 바로 환불한다.
  const refunded = rows.filter((row) => row.type === "refunded");

  return (
    <>
      <div className={kit.grid3}>
        <div className={`${kit.card} ${kit.stat}`}><span className={kit.statLabel}>환불 완료</span><span className={kit.statValue}>{refunded.length}건</span></div>
        <div className={`${kit.card} ${kit.stat}`}><span className={kit.statLabel}>최근 충전액</span><span className={kit.statValue}>{formatWon(rows.filter((r) => r.type === "charge").reduce((s, r) => s + chipsToWon(r.chips), 0))}</span></div>
        <div className={`${kit.card} ${kit.stat}`}><span className={kit.statLabel}>칩 단가</span><span className={kit.statValue}>{formatWon(CHIP_PRICE_WON)}</span></div>
      </div>
      <section className={kit.card}>
        <div className={kit.tableWrap}>
          <table className={kit.table}>
            <thead>
              <tr>
                <th scope="col">일시</th><th scope="col">회원</th><th scope="col">구분</th>
                <th scope="col" className={kit.num}>칩</th><th scope="col" className={kit.num}>금액</th><th scope="col">메모</th>
                <th scope="col"><span className="sr-only">관리</span></th>
              </tr>
            </thead>
            <tbody>
              {rows.map((row) => (
                <tr key={row.id}>
                  <td>{formatDate(row.createdAt)}</td>
                  <td className={kit.strong}>{row.user}</td>
                  <td><StatusPill status={row.type} /></td>
                  <td className={kit.num}>{formatCount(row.chips)}</td>
                  <td className={kit.num}>{formatWon(chipsToWon(row.chips))}</td>
                  <td>{row.memo}</td>
                  <td>
                    {row.type === "charge" && (
                      <ConfirmButton
                        label="환불"
                        title={`${row.user}님 충전을 환불할까요?`}
                        description={`${formatCount(row.chips)}칩을 회수하고 환불됨으로 바꿔요. 회원이 칩을 이미 썼으면 환불되지 않아요. (실제 ${formatWon(chipsToWon(row.chips))} 송금은 PG 연동 후)`}
                        confirmLabel="환불"
                        className={`${kit.btnPrimary} ${kit.sm}`}
                        onConfirm={approveRefund.bind(null, row.id)}
                      />
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        {rows.length === 0 && <p className={kit.empty}>아직 충전·환불 내역이 없어요.</p>}
      </section>
    </>
  );
}

export default function AdminCoinsPage() {
  return (
    <>
      <h1 className={kit.pageTitle}>충전·환불 관리</h1>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <CoinsContent />
      </Suspense>
    </>
  );
}
