import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { connection } from "next/server";
import { Suspense } from "react";
import { Avatar } from "@/app/_components/ui/Avatar";
import { ConfirmButton } from "@/app/_components/ui/ConfirmButton";
import kit from "@/app/_components/ui/kit.module.css";
import { LinkTabs } from "@/app/_components/ui/LinkTabs";
import { Pagination } from "@/app/_components/ui/Pagination";
import { changeUserRole } from "../_actions";
import { getAdminAccess } from "../_lib/access";
import { getRoleUsers, type RoleFilter, type RoleUser } from "../_lib/api";
import styles from "../_components/Admin.module.css";

export const metadata: Metadata = { title: "권한 관리" };

const filters: { value: RoleFilter; label: string }[] = [
  { value: undefined, label: "전체" },
  { value: "ADMIN", label: "관리자" },
  { value: "USER", label: "일반 회원" },
];

const roleLabels: Record<RoleUser["role"], { label: string; className: string }> = {
  MASTER: { label: "마스터", className: kit.pillAccent },
  ADMIN: { label: "관리자", className: kit.pillSuccess },
  USER: { label: "일반 회원", className: kit.pill },
};

function first(value: string | string[] | undefined) {
  return Array.isArray(value) ? value[0] : value;
}

async function RolesContent({ searchParams }: { searchParams: Promise<Record<string, string | string[] | undefined>> }) {
  await connection();
  // 최고 관리자만 볼 수 있다. 일반 관리자에게는 페이지가 있다는 것도 드러내지 않는다.
  if ((await getAdminAccess()) !== "master") notFound();

  const params = await searchParams;
  const q = first(params.q)?.trim().slice(0, 30) || undefined;
  const role = filters.find((item) => item.value && item.value === first(params.role))?.value;
  const page = Math.max(1, Number(first(params.page)) || 1);
  const result = await getRoleUsers({ q, role, page });
  const hrefFor = (target: number, nextRole: RoleFilter = role) => {
    const query = new URLSearchParams();
    if (q) query.set("q", q);
    if (nextRole) query.set("role", nextRole);
    if (target > 1) query.set("page", String(target));
    const search = query.toString();
    return search ? `/admin/roles?${search}` : "/admin/roles";
  };

  return (
    <section className={kit.card}>
      <LinkTabs
        label="권한 종류"
        variant="line"
        items={filters.map((item) => ({ href: hrefFor(1, item.value), label: item.label, active: item.value === role }))}
      />
      <form action="/admin/roles" method="get" className={styles.filters} role="search" style={{ marginTop: 16 }}>
        {role && <input type="hidden" name="role" value={role} />}
        <div className={`${kit.field} ${styles.field}`}>
          <label htmlFor="role-q" className={kit.label}>
            닉네임·이메일
          </label>
          <input id="role-q" name="q" type="search" defaultValue={q} className={kit.input} />
        </div>
        <button type="submit" className={kit.btnDark}>
          검색
        </button>
      </form>
      <p className={kit.faint} role="status" style={{ margin: "16px 0 8px" }}>
        총 {result.totalElements}명 · 바꾼 권한은 그 회원이 다시 로그인하거나 토큰이 갱신될 때(최대 Access Token 유효 시간 안) 적용돼요.
      </p>
      <div className={kit.tableWrap}>
        <table className={kit.table}>
          <thead>
            <tr>
              <th scope="col">ID</th>
              <th scope="col">회원</th>
              <th scope="col">이메일</th>
              <th scope="col">권한</th>
              <th scope="col">변경</th>
            </tr>
          </thead>
          <tbody>
            {result.content.map((user) => {
              const pill = roleLabels[user.role];
              return (
                <tr key={user.id}>
                  <td>{user.id}</td>
                  <td>
                    <span className={kit.row}>
                      <Avatar name={user.nickname} size={28} tone="light" />
                      <strong>{user.nickname}</strong>
                    </span>
                  </td>
                  <td>{user.email}</td>
                  <td>
                    <span className={pill.className}>{pill.label}</span>
                  </td>
                  <td>
                    {user.role === "USER" && (
                      <ConfirmButton
                        label="관리자로 지정"
                        title={`${user.nickname}님을 관리자로 지정할까요?`}
                        description="관리자 화면의 모든 메뉴(회원 제재, 콘텐츠 삭제, 정산 등)를 쓸 수 있게 돼요."
                        confirmLabel="지정"
                        className={`${kit.btn} ${kit.sm}`}
                        onConfirm={changeUserRole.bind(null, user.id, "ADMIN")}
                      />
                    )}
                    {user.role === "ADMIN" && (
                      <ConfirmButton
                        label="일반 회원으로 강등"
                        title={`${user.nickname}님의 관리자 권한을 회수할까요?`}
                        description="관리자 화면에 더 이상 들어올 수 없어요."
                        confirmLabel="강등"
                        danger
                        className={`${kit.btnGhost} ${kit.sm}`}
                        onConfirm={changeUserRole.bind(null, user.id, "USER")}
                      />
                    )}
                    {user.role === "MASTER" && <span className={kit.faint}>변경 불가</span>}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
      {result.content.length === 0 && <p className={kit.empty}>조건에 맞는 회원이 없어요.</p>}
      <Pagination page={page} totalPages={result.totalPages} hrefFor={(target) => hrefFor(target)} />
    </section>
  );
}

export default function AdminRolesPage({ searchParams }: PageProps<"/admin/roles">) {
  return (
    <>
      <h1 className={kit.pageTitle}>권한 관리</h1>
      <p className={kit.muted} style={{ marginBottom: 16 }}>
        최고 관리자만 볼 수 있는 화면이에요. 회원을 관리자로 지정하거나 일반 회원으로 되돌릴 수 있어요.
      </p>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <RolesContent searchParams={searchParams} />
      </Suspense>
    </>
  );
}
