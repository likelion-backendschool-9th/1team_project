"use client";

import { useState } from "react";
import kit from "@/app/_components/ui/kit.module.css";
import { formatCount, formatDateTime } from "@/app/_lib/format";
import type { ChipHistory } from "../../_lib/mock";
import type { HistoryPage } from "../../_lib/api";
import styles from "../../_components/My.module.css";
import { loadHistoryPage } from "../_actions";

type Props = {
  initial: HistoryPage;
};

const historyLabels = { charge: "충전", use: "사용", refund: "환불", revenue: "수익" } as const;

// 첫 페이지는 서버에서 받아 오고, 이전·다음 버튼으로 페이지를 이동함
export function ChipHistoryList({ initial }: Props) {
  const [items, setItems] = useState<ChipHistory[]>(initial.items);
  const [page, setPage] = useState(initial.page);
  const [totalPages, setTotalPages] = useState(initial.totalPages);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(false);

  async function goTo(target: number) {
    if (loading) return;
    setLoading(true);
    setError(false);
    try {
      const next = await loadHistoryPage(target);
      setItems(next.items);
      setPage(next.page);
      setTotalPages(next.totalPages);
    } catch {
      setError(true);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className={kit.tableWrap}>
      <table className={kit.table}>
        <thead>
          <tr>
            <th scope="col">일시</th>
            <th scope="col">구분</th>
            <th scope="col">내용</th>
            <th scope="col" className={kit.num}>칩</th>
          </tr>
        </thead>
        <tbody>
          {items.map((row) => (
            <tr key={row.id}>
              <td>{formatDateTime(row.createdAt)}</td>
              <td>
                <span
                  className={
                    row.type === "use"
                      ? kit.pill
                      : row.type === "charge"
                        ? kit.pillAccent
                        : row.type === "revenue"
                          ? kit.pillWarn
                          : kit.pillSuccess
                  }
                >
                  {historyLabels[row.type]}
                </span>
              </td>
              <td>{row.description}</td>
              <td className={`${kit.num} ${row.amount > 0 ? styles.plus : styles.minus}`}>
                {row.amount > 0 ? "+" : ""}
                {formatCount(row.amount)}
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {items.length === 0 && (
        <p className={kit.empty}>아직 거래 내역이 없어요.</p>
      )}

      {/* 에러 메시지 — totalPages와 무관하게 항상 표시 */}
      {error && (
        <p role="status" className={kit.faint} style={{ textAlign: "center", paddingTop: 12 }}>
          내역을 불러오지 못했어요.
        </p>
      )}

      {/* 페이지 이동 */}
      {totalPages > 1 && (
        <div className={styles.historyFooter}>
          <button
            type="button"
            className={`${kit.btnGhost} ${kit.sm}`}
            onClick={() => goTo(page - 1)}
            disabled={loading || page <= 1}
          >
            이전
          </button>
          <span className={kit.faint} aria-live="polite">
            {loading ? "불러오는 중…" : `${page} / ${totalPages}`}
          </span>
          <button
            type="button"
            className={`${kit.btnGhost} ${kit.sm}`}
            onClick={() => goTo(page + 1)}
            disabled={loading || page >= totalPages}
          >
            다음
          </button>
        </div>
      )}
    </div>
  );
}
