"use client";

import { useCallback, useEffect, useRef, useState } from "react";
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

// 첫 페이지는 서버에서 받아 오고, 맨 아래에 닿으면 다음 페이지를 이어 붙임
export function ChipHistoryList({ initial }: Props) {
  const [items, setItems] = useState<ChipHistory[]>(initial.items);
  const [page, setPage] = useState(initial.page);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(false);
  const loadingRef = useRef(false);
  const moreRef = useRef<HTMLButtonElement>(null);
  const hasMore = page < initial.totalPages;

  const loadMore = useCallback(async () => {
    if (loadingRef.current) return;
    loadingRef.current = true;
    setLoading(true);
    setError(false);
    try {
      const next = await loadHistoryPage(page + 1);
      setItems((prev) => {
        // 그 사이 내역이 추가돼도 중복 방지
        const seen = new Set(prev.map((item) => item.id));
        return [...prev, ...next.items.filter((item) => !seen.has(item.id))];
      });
      setPage(next.page);
    } catch {
      setError(true);
    } finally {
      loadingRef.current = false;
      setLoading(false);
    }
  }, [page]);

  // "더 보기" 버튼이 화면에 보이면 자동으로 누름. 키보드 사용자는 버튼을 직접 누를 수 있음
  useEffect(() => {
    const button = moreRef.current;
    if (!button || !hasMore || error) return;
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) void loadMore();
      },
      { rootMargin: "200px 0px" },
    );
    observer.observe(button);
    return () => observer.disconnect();
  }, [hasMore, error, loadMore]);

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
              <td
                className={`${kit.num} ${row.amount > 0 ? styles.plus : styles.minus}`}
              >
                {row.amount > 0 ? "+" : ""}
                {formatCount(row.amount)}
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {/* 목록이 비어 있을 때 */}
      {items.length === 0 && (
        <p className={kit.empty}>아직 거래 내역이 없어요.</p>
      )}

      {/* 페이지네이션 푸터 */}
      <div className={styles.historyFooter}>
        <p role="status" className={kit.faint}>
          {loading
            ? "불러오는 중…"
            : error
              ? "내역을 불러오지 못했어요."
              : !hasMore && items.length > 0
                ? `${formatCount(items.length)}건을 모두 불러왔어요.`
                : ""}
        </p>
        {hasMore && (
          <button
            ref={moreRef}
            type="button"
            className={`${kit.btnGhost} ${kit.sm}`}
            onClick={loadMore}
            disabled={loading}
          >
            {error ? "다시 시도" : "더 보기"}
          </button>
        )}
      </div>
    </div>
  );
}
