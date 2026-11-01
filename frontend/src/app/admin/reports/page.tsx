import type { Metadata } from "next";
import Link from "next/link";
import { connection } from "next/server";
import { Suspense } from "react";
import kit from "@/app/_components/ui/kit.module.css";
import { LinkTabs } from "@/app/_components/ui/LinkTabs";
import { formatDate } from "@/app/_lib/format";
import { StatusPill } from "../_components/StatusPill";
import { getReports, REPORT_TYPES } from "../_lib/api";

export const metadata: Metadata = { title: "신고 관리" };

const filters = [
  { value: undefined, label: "전체" },
  { value: "pending", label: "대기" },
  { value: "resolved", label: "처리 완료" },
  { value: "rejected", label: "기각" },
];

async function ReportsContent({ searchParams }: { searchParams: Promise<Record<string, string | string[] | undefined>> }) {
  await connection();
  const params = await searchParams;
  const status = filters.find((item) => item.value && item.value === params.status)?.value;
  const type = REPORT_TYPES.find((item) => item.value === params.type)?.value;
  const reports = await getReports(status, type);
  const href = (nextStatus: string | undefined, nextType: string | undefined) => {
    const query = new URLSearchParams();
    if (nextStatus) query.set("status", nextStatus);
    if (nextType) query.set("type", nextType);
    const search = query.toString();
    return search ? `/admin/reports?${search}` : "/admin/reports";
  };

  return (
    <>
      <LinkTabs
        label="신고 상태"
        items={filters.map((item) => ({ href: href(item.value, type), label: item.label, active: item.value === status }))}
      />
      <LinkTabs
        label="신고 유형"
        variant="line"
        items={[
          { href: href(status, undefined), label: "전체 유형", active: !type },
          ...REPORT_TYPES.map((item) => ({ href: href(status, item.value), label: item.label, active: item.value === type })),
        ]}
      />
      <section className={kit.card}>
        <div className={kit.tableWrap}>
          <table className={kit.table}>
            <thead>
              <tr>
                <th scope="col">접수일</th>
                <th scope="col">대상</th>
                <th scope="col">사유</th>
                <th scope="col">신고자 → 피신고자</th>
                <th scope="col">상태</th>
              </tr>
            </thead>
            <tbody>
              {reports.map((report) => (
                <tr key={report.id}>
                  <td>{formatDate(report.createdAt)}</td>
                  <td>
                    <Link href={`/admin/reports/${report.id}`} className={kit.strong}>
                      [{report.targetType}] {report.targetTitle}
                    </Link>
                  </td>
                  <td>{report.reason}</td>
                  <td>
                    {report.reporter} → {report.reported}
                  </td>
                  <td>
                    <StatusPill status={report.status} />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        {reports.length === 0 && <p className={kit.empty}>조건에 맞는 신고가 없어요.</p>}
      </section>
    </>
  );
}

export default function AdminReportsPage({ searchParams }: PageProps<"/admin/reports">) {
  return (
    <>
      <header>
        <h1 className={kit.pageTitle}>신고 관리</h1>
        <p className={kit.pageDesc}>처리 대기 중인 신고가 먼저 보여요.</p>
      </header>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <ReportsContent searchParams={searchParams} />
      </Suspense>
    </>
  );
}
