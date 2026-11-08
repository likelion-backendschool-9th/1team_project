import { connection } from "next/server";
import { ConfirmButton } from "@/app/_components/ui/ConfirmButton";
import kit from "@/app/_components/ui/kit.module.css";
import { Pagination } from "@/app/_components/ui/Pagination";
import { formatDate } from "@/app/_lib/format";
import { deleteContent } from "../_actions";
import { getContent } from "../_lib/api";
import type { ContentKind } from "../_lib/mock";
import { StatusPill } from "./StatusPill";
import styles from "./Admin.module.css";

type Props = {
  kind: ContentKind;
  noun: string;
  searchParams: Promise<Record<string, string | string[] | undefined>>;
};


export async function ContentTable({ kind, noun, searchParams }: Props) {
  await connection();
  const params = await searchParams;
  const raw = params.q;
  const q = (Array.isArray(raw) ? raw[0] : raw)?.trim().slice(0, 30) || undefined;
  const pageRaw = params.page;
  const page = Math.max(1, Number(Array.isArray(pageRaw) ? pageRaw[0] : pageRaw) || 1);
  const result = await getContent(kind, { q, page });
  const hrefFor = (target: number) => {
    const query = new URLSearchParams();
    if (q) query.set("q", q);
    if (target > 1) query.set("page", String(target));
    const search = query.toString();
    return search ? `/admin/${kind}?${search}` : `/admin/${kind}`;
};

  return (
    <section className={kit.card}>
      <form action={`/admin/${kind}`} method="get" role="search" className={styles.filters}>
        <div className={`${kit.field} ${styles.field}`}>
          <label htmlFor={`${kind}-q`} className={kit.label}>
            제목·작성자
          </label>
          <input id={`${kind}-q`} name="q" type="search" defaultValue={q} className={kit.input} />
        </div>
        <button type="submit" className={kit.btnDark}>
          검색
        </button>
      </form>
      <p className={kit.faint} role="status" style={{ margin: "16px 0 8px" }}>
        {noun} {result.totalElements}개 · 신고가 많은 항목은 빨간색으로 표시돼요.
      </p>
      <div className={kit.tableWrap}>
        <table className={kit.table}>
          <thead>
            <tr>
              <th scope="col">ID</th>
              <th scope="col">내용</th>
              <th scope="col">작성자</th>
              <th scope="col">작성일</th>
              <th scope="col">지표</th>
              <th scope="col" className={kit.num}>신고</th>
              <th scope="col">상태</th>
              <th scope="col"><span className="sr-only">관리</span></th>
            </tr>
          </thead>
          <tbody>
            {result.content.map((row) => (
              <tr key={row.id}>
                <td>{row.id}</td>
                <td className={kit.strong}>{row.title}</td>
                <td>{row.author}</td>
                <td>{formatDate(row.createdAt)}</td>
                <td>{row.metric}</td>
                <td className={kit.num}>
                  {row.reported > 0 ? <span className={kit.pillDanger}>{row.reported}</span> : 0}
                </td>
                <td>
                  <StatusPill status={row.status} />
                </td>
                <td>
                  <ConfirmButton
                    label="삭제"
                    title={`이 ${noun}을(를) 삭제할까요?`}
                    description={`「${row.title}」 · 목록에서 숨기고 관리자 기록에 남깁니다.`}
                    confirmLabel="삭제"
                    danger
                    className={`${kit.btnGhost} ${kit.sm}`}
                    onConfirm={deleteContent.bind(null, kind, row.id)}
                  />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {result.content.length === 0 && <p className={kit.empty}>조건에 맞는 {noun}이(가) 없어요.</p>}
      <Pagination page={page} totalPages={result.totalPages} hrefFor={hrefFor} />
    </section>
  );
}
