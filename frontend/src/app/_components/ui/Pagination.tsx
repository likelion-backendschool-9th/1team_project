import Link from "next/link";
import kit from "./kit.module.css";

type Props = {
  page: number;
  totalPages: number;
  hrefFor: (page: number) => string;
};

/** 지금 페이지 앞뒤로 보여 줄 번호 수 */
const AROUND = 2;

// 1 … 4 5 [6] 7 8 … 20 처럼 처음·끝과 지금 페이지 주변만 보여 준다.
function visiblePages(page: number, totalPages: number): (number | "gap")[] {
  const start = Math.max(1, page - AROUND);
  const end = Math.min(totalPages, page + AROUND);
  const pages: (number | "gap")[] = [];
  if (start > 1) pages.push(1);
  if (start > 2) pages.push("gap");
  for (let value = start; value <= end; value += 1) pages.push(value);
  if (end < totalPages - 1) pages.push("gap");
  if (end < totalPages) pages.push(totalPages);
  return pages;
}

export function Pagination({ page, totalPages, hrefFor }: Props) {
  if (totalPages <= 1) return null;
  const current = Math.min(Math.max(page, 1), totalPages);

  return (
    <nav aria-label="페이지 이동" className={kit.pagination}>
      {current > 1 && (
        <Link href={hrefFor(current - 1)} className={kit.pageLink} aria-label="이전 페이지">
          ‹
        </Link>
      )}
      {visiblePages(current, totalPages).map((value, index) =>
        value === "gap" ? (
          <span key={`gap-${index}`} className={kit.pageGap} aria-hidden="true">
            …
          </span>
        ) : (
          <Link
            key={value}
            href={hrefFor(value)}
            className={`${kit.pageLink} ${value === current ? kit.pageCurrent : ""}`}
            aria-current={value === current ? "page" : undefined}
          >
            {value}
          </Link>
        ),
      )}
      {current < totalPages && (
        <Link href={hrefFor(current + 1)} className={kit.pageLink} aria-label="다음 페이지">
          ›
        </Link>
      )}
    </nav>
  );
}
