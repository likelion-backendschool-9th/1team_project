import type { Metadata } from "next";
import Link from "next/link";
import { connection } from "next/server";
import { Suspense } from "react";
import kit from "@/app/_components/ui/kit.module.css";
import { Pagination } from "@/app/_components/ui/Pagination";
import { formatCount, formatDate } from "@/app/_lib/format";
import { getUsers } from "../_lib/api";
import { StatusPill } from "../_components/StatusPill";
import styles from "../_components/Admin.module.css";

export const metadata: Metadata = { title: "회원 관리" };

const statuses = [
  { value: "", label: "전체 상태" },
  { value: "active", label: "정상" },
  { value: "suspended", label: "정지" },
  { value: "withdrawn", label: "탈퇴" },
];

function first(value: string | string[] | undefined) {
  return Array.isArray(value) ? value[0] : value;
}

async function UsersContent({ searchParams }: { searchParams: Promise<Record<string, string | string[] | undefined>> }) {
  await connection();
  const params = await searchParams;
  const q = first(params.q)?.trim().slice(0, 30) || undefined;
  const status = statuses.find((item) => item.value && item.value === first(params.status))?.value;
  const page = Math.max(1, Number(first(params.page)) || 1);
  const result = await getUsers({ q, status, page });
  const hrefFor = (target: number) => {
    const query = new URLSearchParams();
    if (q) query.set("q", q);
    if (status) query.set("status", status);
    if (target > 1) query.set("page", String(target));
    const search = query.toString();
    return search ? `/admin/users?${search}` : "/admin/users";
  };

  return (
    <section className={kit.card}>
      <form action="/admin/users" method="get" className={styles.filters} role="search">
        <div className={`${kit.field} ${styles.field}`}>
          <label htmlFor="user-q" className={kit.label}>
            닉네임·이메일
          </label>
          <input id="user-q" name="q" type="search" defaultValue={q} className={kit.input} />
        </div>
        <div className={`${kit.field} ${styles.field}`}>
          <label htmlFor="user-status" className={kit.label}>
            상태
          </label>
          <select id="user-status" name="status" defaultValue={status ?? ""} className={kit.select}>
            {statuses.map((item) => (
              <option key={item.value} value={item.value}>
                {item.label}
              </option>
            ))}
          </select>
        </div>
        <button type="submit" className={kit.btnDark}>
          검색
        </button>
      </form>
      <p className={kit.faint} role="status" style={{ margin: "16px 0 8px" }}>
        총 {result.totalElements}명
      </p>
      <div className={kit.tableWrap}>
        <table className={kit.table}>
          <thead>
            <tr>
              <th scope="col">ID</th>
              <th scope="col">닉네임</th>
              <th scope="col">이메일</th>
              <th scope="col">가입</th>
              <th scope="col" className={kit.num}>데모/글</th>
              <th scope="col" className={kit.num}>신고</th>
              <th scope="col">상태</th>
            </tr>
          </thead>
          <tbody>
            {result.content.map((user) => (
              <tr key={user.id}>
                <td>{user.id}</td>
                <td>
                  <Link href={`/admin/users/${user.id}`} className={kit.strong}>
                    {user.nickname}
                  </Link>
                </td>
                <td>{user.email}</td>
                <td>{formatDate(user.joinedAt)}</td>
                <td className={kit.num}>
                  {formatCount(user.demoCount)} / {formatCount(user.postCount)}
                </td>
                <td className={kit.num}>{user.reportCount}</td>
                <td>
                  <StatusPill status={user.status} />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {result.content.length === 0 && <p className={kit.empty}>조건에 맞는 회원이 없어요.</p>}
      <Pagination page={page} totalPages={result.totalPages} hrefFor={hrefFor} />
    </section>
  );
}

export default function AdminUsersPage({ searchParams }: PageProps<"/admin/users">) {
  return (
    <>
      <h1 className={kit.pageTitle}>회원 관리</h1>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <UsersContent searchParams={searchParams} />
      </Suspense>
    </>
  );
}
