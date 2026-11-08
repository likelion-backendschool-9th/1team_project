"use client";

import { type ChangeEvent } from "react";
import { useRouter } from "next/navigation";
import kit from "@/app/_components/ui/kit.module.css";
import type { SearchSort } from "../_lib/api";
import styles from "./SearchContent.module.css";

type Props = {
  value: SearchSort;
  options: { value: SearchSort; label: string; href: string }[];
};

/** 검색 결과 정렬. 다른 값을 고르면 바로 적용한다 */
export function SearchSortSelect({ value, options }: Props) {
  const router = useRouter();

  function handleChange(event: ChangeEvent<HTMLSelectElement>) {
    const selected = options.find((option) => option.value === event.target.value);
    if (selected) router.push(selected.href);
  }

  return (
    <div className={styles.sortForm}>
      <label htmlFor="search-sort" className={kit.label}>
        정렬
      </label>
      <select id="search-sort" key={value} defaultValue={value} onChange={handleChange} className={`${kit.select} ${styles.sortSelect}`}>
        {options.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
    </div>
  );
}
