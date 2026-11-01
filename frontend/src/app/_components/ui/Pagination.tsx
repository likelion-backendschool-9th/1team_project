import Link from "next/link";
import kit from "./kit.module.css";

type Props = {
  page: number;
  totalPages: number;
  hrefFor: (page: number) => string;
};

export function Pagination({ page, totalPages, hrefFor }: Props) {
  if (totalPages <= 1) return null;
  const pages = Array.from({ length: totalPages }, (_, index) => index + 1);

  return (
    <nav aria-label="페이지 이동" className={kit.pagination}>
      {pages.map((value) => (
        <Link
          key={value}
          href={hrefFor(value)}
          className={`${kit.pageLink} ${value === page ? kit.pageCurrent : ""}`}
          aria-current={value === page ? "page" : undefined}
        >
          {value}
        </Link>
      ))}
    </nav>
  );
}
