import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { connection } from "next/server";
import { Suspense } from "react";
import kit from "@/app/_components/ui/kit.module.css";
import { formatDate } from "@/app/_lib/format";
import { processReport } from "../../_actions";
import { DecisionForm } from "../../_components/DecisionForm";
import { StatusPill } from "../../_components/StatusPill";
import { getReport } from "../../_lib/api";
import styles from "../../_components/Admin.module.css";

export const metadata: Metadata = { title: "신고 처리" };

async function ReportDetail({ params }: { params: Promise<{ reportId: string }> }) {
  await connection();
  const reportId = Number((await params).reportId);
  const report = Number.isSafeInteger(reportId) && reportId > 0 ? await getReport(reportId) : null;
  if (!report) notFound();

  return (
    <>
      <section className={kit.card} aria-labelledby="report-info">
        <div className={kit.cardHeader}>
          <h2 id="report-info" className={kit.cardTitle}>
            신고 #{report.id} <StatusPill status={report.status} />
          </h2>
          <Link href={report.targetHref} className={kit.more} target="_blank" rel="noopener">
            대상 보기 ↗<span className="sr-only">(새 창)</span>
          </Link>
        </div>
        <dl className={styles.detail}>
          <div><dt>대상</dt><dd>[{report.targetType}] {report.targetTitle}</dd></div>
          <div><dt>사유</dt><dd>{report.reason}</dd></div>
          <div><dt>신고자</dt><dd>{report.reporter}</dd></div>
          <div><dt>피신고자</dt><dd>{report.reported}</dd></div>
          <div><dt>접수일</dt><dd>{formatDate(report.createdAt)}</dd></div>
        </dl>
        <p className={kit.muted} style={{ marginTop: 16, padding: 14, borderRadius: 12, background: "#fbfaf7" }}>
          “{report.detail}”
        </p>
      </section>
      {report.status === "pending" ? (
        <DecisionForm
          action={processReport.bind(null, report.id)}
          title="신고 처리"
          choiceName="decision"
          choiceLabel="처리 방식"
          choices={[
            { value: "hide", label: "콘텐츠 숨김" },
            { value: "sanction", label: "숨김 + 회원 제재", danger: true },
            { value: "reject", label: "기각" },
          ]}
          reasonName="memo"
          reasonLabel="처리 사유 (관리자 기록용)"
          submitLabel="처리 완료"
        />
      ) : (
        <section className={kit.card} aria-labelledby="report-handled">
          <h2 id="report-handled" className={kit.cardTitle}>
            처리 이력
          </h2>
          <dl className={styles.detail}>
            <div><dt>처리자</dt><dd>{report.handledBy ?? "-"}</dd></div>
            <div><dt>처리 시각</dt><dd>{report.handledAt ? formatDate(report.handledAt) : "-"}</dd></div>
            <div><dt>메모</dt><dd>{report.adminNote || "-"}</dd></div>
          </dl>
        </section>
      )}
    </>
  );
}

export default function AdminReportPage({ params }: PageProps<"/admin/reports/[reportId]">) {
  return (
    <>
      <Link href="/admin/reports" className={kit.more}>← 신고 목록</Link>
      <h1 className={kit.pageTitle}>신고 처리</h1>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <ReportDetail params={params} />
      </Suspense>
    </>
  );
}
