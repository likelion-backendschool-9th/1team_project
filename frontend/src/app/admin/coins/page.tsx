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
  const requests = rows.filter((row) => row.type === "refund-request");

  return (
    <>
      <div className={kit.grid3}>
        <div className={`${kit.card} ${kit.stat}`}><span className={kit.statLabel}>환불 요청</span><span className={kit.statValue}>{requests.length}건</span></div>
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
                    {row.type === "refund-request" && (
                      <ConfirmButton
                        label="환불 승인"
                        title={`${row.user}님 환불을 승인할까요?`}
                        description={`${formatCount(row.chips)}칩 회수 후 ${formatWon(chipsToWon(row.chips))}을 환불해요.`}
                        confirmLabel="승인"
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
