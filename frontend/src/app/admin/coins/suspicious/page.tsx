import type { Metadata } from "next";
import Link from "next/link";
import { Suspense } from "react";
import { ConfirmButton } from "@/app/_components/ui/ConfirmButton";
import kit from "@/app/_components/ui/kit.module.css";
import { formatDate, formatWon } from "@/app/_lib/format";
import { suspendForFraud } from "../../_actions";
import { StatusPill } from "../../_components/StatusPill";
import { getSuspicious } from "../../_lib/api";

export const metadata: Metadata = { title: "부정 사용 확인" };

async function SuspiciousContent() {
  const rows = await getSuspicious();
  if (rows.length === 0) {
    return (
      <p className={kit.empty}>
        지금 감지된 이상 거래가 없어요. (24시간 안 충전 5회 이상, 30일 안 환불 2회 이상, 7일 안 같은 사람에게 후원 5회 이상)
      </p>
    );
  }

  return (
    <ul className={kit.stack}>
      {rows.map((row) => (
        <li key={row.id} className={kit.card}>
          <div className={kit.between}>
            <p className={kit.row}>
              <StatusPill status={row.level} />
              <Link href={`/admin/users/${row.userId}`} className={kit.strong}>{row.user}</Link>
              <span className={kit.faint}>{formatDate(row.detectedAt)} 감지</span>
            </p>
            <ConfirmButton
              label="계정 정지"
              title={`${row.user}님 계정을 정지할까요?`}
              description="영구 정지되고 진행 중인 결제가 모두 막혀요."
              confirmLabel="정지"
              danger
              className={`${kit.btnDanger} ${kit.sm}`}
              onConfirm={suspendForFraud.bind(null, row.userId)}
            />
          </div>
          <p className={kit.strong} style={{ marginTop: 10 }}>{row.pattern}</p>
          <p className={kit.faint}>발생 {row.count}회 · 관련 금액 {formatWon(row.amount)}</p>
        </li>
      ))}
    </ul>
  );
}

export default function AdminSuspiciousPage() {
  return (
    <>
      <header>
        <h1 className={kit.pageTitle}>부정 사용 확인</h1>
        <p className={kit.pageDesc}>결제·환불 패턴에서 이상 징후가 감지된 계정이에요.</p>
      </header>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <SuspiciousContent />
      </Suspense>
    </>
  );
}
