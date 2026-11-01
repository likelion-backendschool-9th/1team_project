import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { connection } from "next/server";
import { Suspense } from "react";
import { ConfirmButton } from "@/app/_components/ui/ConfirmButton";
import kit from "@/app/_components/ui/kit.module.css";
import { formatCount, formatDate } from "@/app/_lib/format";
import { releaseUser, sanctionUser } from "../../_actions";
import { DecisionForm } from "../../_components/DecisionForm";
import { StatusPill } from "../../_components/StatusPill";
import { getUser } from "../../_lib/api";
import styles from "../../_components/Admin.module.css";

export const metadata: Metadata = { title: "회원 상세" };

const providers = { github: "GitHub", kakao: "카카오", google: "구글" };

async function UserDetail({ params }: { params: Promise<{ userId: string }> }) {
  await connection();
  const userId = Number((await params).userId);
  const result = Number.isSafeInteger(userId) && userId > 0 ? await getUser(userId) : null;
  if (!result) notFound();
  const { user, sanctions } = result;

  return (
    <>
      <section className={kit.card} aria-labelledby="user-info">
        <div className={kit.cardHeader}>
          <h2 id="user-info" className={kit.cardTitle}>
            {user.nickname} <StatusPill status={user.status} />
          </h2>
          {user.status === "suspended" && (
            <ConfirmButton
              label="제재 해제"
              title={`${user.nickname}님의 제재를 해제할까요?`}
              confirmLabel="해제"
              className={`${kit.btnGhost} ${kit.sm}`}
              onConfirm={releaseUser.bind(null, user.id)}
            />
          )}
        </div>
        <dl className={styles.detail}>
          <div><dt>회원 ID</dt><dd>{user.id}</dd></div>
          <div><dt>이메일</dt><dd>{user.email}</dd></div>
          <div><dt>가입 경로</dt><dd>{providers[user.provider]}</dd></div>
          <div><dt>가입일</dt><dd>{formatDate(user.joinedAt)}</dd></div>
          <div><dt>데모 / 글</dt><dd>{user.demoCount} / {user.postCount}</dd></div>
          <div><dt>받은 신고</dt><dd>{user.reportCount}건</dd></div>
          <div><dt>보유 칩</dt><dd>{formatCount(user.chipBalance)}</dd></div>
          <div><dt>미니홈피</dt><dd><Link href={`/mini-home/${user.id}`} className={kit.more}>바로가기 →</Link></dd></div>
        </dl>
      </section>
      <DecisionForm
        action={sanctionUser.bind(null, user.id)}
        title="제재하기"
        choiceName="period"
        choiceLabel="제재 기간"
        choices={[
          { value: "1일", label: "1일" },
          { value: "7일", label: "7일" },
          { value: "30일", label: "30일" },
          { value: "영구", label: "영구 정지", danger: true },
        ]}
        reasonName="reason"
        reasonLabel="제재 사유 (회원에게 안내돼요)"
        submitLabel="제재 적용"
      />
      <section className={kit.card} aria-labelledby="sanction-history">
        <h2 id="sanction-history" className={kit.cardTitle}>
          제재 이력
        </h2>
        <div className={kit.tableWrap}>
          <table className={kit.table}>
            <thead>
              <tr><th scope="col">일시</th><th scope="col">기간</th><th scope="col">사유</th><th scope="col">처리자</th></tr>
            </thead>
            <tbody>
              {sanctions.map((item) => (
                <tr key={item.id}>
                  <td>{formatDate(item.createdAt)}</td>
                  <td>{item.period}</td>
                  <td>{item.reason}</td>
                  <td>{item.admin}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </>
  );
}

export default function AdminUserPage({ params }: PageProps<"/admin/users/[userId]">) {
  return (
    <>
      <Link href="/admin/users" className={kit.more}>← 회원 목록</Link>
      <h1 className={kit.pageTitle}>회원 상세</h1>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <UserDetail params={params} />
      </Suspense>
    </>
  );
}
