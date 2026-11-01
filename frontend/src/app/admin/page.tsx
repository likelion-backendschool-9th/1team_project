import type { Metadata } from "next";
import Link from "next/link";
import { Suspense } from "react";
import kit from "@/app/_components/ui/kit.module.css";
import { formatCount, formatDate, formatWon } from "@/app/_lib/format";
import { getDashboard, getReports } from "./_lib/api";
import styles from "./_components/Admin.module.css";

export const metadata: Metadata = { title: "대시보드" };

const days = ["월", "화", "수", "목", "금", "토", "일"];

async function DashboardContent() {
  const [stats, reports] = await Promise.all([getDashboard(), getReports("pending")]);
  const max = Math.max(...stats.signups);
  const cards = [
    { label: "전체 회원", value: `${formatCount(stats.userCount)}명`, delta: `오늘 +${stats.newUsersToday}` },
    { label: "이번 주 데모 업로드", value: `${formatCount(stats.demoUploadsWeek)}개`, delta: "지난주 대비 +12%" },
    { label: "처리 대기 신고", value: `${stats.pendingReports}건`, delta: "대기 우선 처리" },
    { label: "지난달 매출", value: formatWon(stats.revenueMonth), delta: "구독 + 칩 충전" },
  ];

  return (
    <>
      <div className={kit.grid4}>
        {cards.map((card) => (
          <div key={card.label} className={`${kit.card} ${kit.stat}`}>
            <span className={kit.statLabel}>{card.label}</span>
            <span className={kit.statValue}>{card.value}</span>
            <span className={kit.statDelta}>{card.delta}</span>
          </div>
        ))}
      </div>
      <div className={kit.grid2}>
        <section className={kit.card} aria-labelledby="signup-chart">
          <h2 id="signup-chart" className={kit.cardTitle}>
            최근 7일 가입자
          </h2>
          <div
            className={styles.chart}
            role="img"
            aria-label={stats.signups.map((value, index) => `${days[index]} ${value}명`).join(", ")}
          >
            {stats.signups.map((value, index) => (
              <div key={days[index]} className={styles.bar}>
                <span className={styles.barValue}>{value}</span>
                <span className={styles.barFill} style={{ height: `${(value / max) * 80}%` }} />
                <span className={kit.faint}>{days[index]}</span>
              </div>
            ))}
          </div>
        </section>
        <section className={kit.card} aria-labelledby="pending-reports">
          <div className={kit.cardHeader}>
            <h2 id="pending-reports" className={kit.cardTitle}>
              처리 대기 신고
            </h2>
            <Link href="/admin/reports?status=pending" className={kit.more}>
              전체 보기 →
            </Link>
          </div>
          <ul className={kit.list}>
            {reports.map((report) => (
              <li key={report.id}>
                <Link href={`/admin/reports/${report.id}`} className={kit.listItem}>
                  <span className={kit.pillDanger}>{report.reason}</span>
                  <span className={`${kit.grow} ${kit.ellipsis}`}>
                    [{report.targetType}] {report.targetTitle}
                  </span>
                  <span className={kit.faint}>{formatDate(report.createdAt)}</span>
                </Link>
              </li>
            ))}
          </ul>
        </section>
      </div>
    </>
  );
}

export default function AdminDashboardPage() {
  return (
    <>
      <header>
        <h1 className={kit.pageTitle}>대시보드</h1>
        <p className={kit.pageDesc}>찍먹 서비스 현황을 한눈에 확인해요.</p>
      </header>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <DashboardContent />
      </Suspense>
    </>
  );
}
