"use client";

import Link from "next/link";
import { type ReactNode, useState, useTransition } from "react";
import kit from "@/app/_components/ui/kit.module.css";
import { hideSearchResult, recordSearchVisit } from "../_actions";
import styles from "./SearchContent.module.css";

type Props = {
  href: string;
  type: "post" | "demo" | "user" | "feed";
  id: number;
  query: string;
  canHide: boolean;
  children: ReactNode;
};

/** 검색 결과 한 줄. 눌러서 들어가면 방문을 기록하고, 로그인 사용자는 결과를 숨길 수 있다 */
export function SearchResultRow({ href, type, id, query, canHide, children }: Props) {
  const [pending, startTransition] = useTransition();
  const [message, setMessage] = useState("");

  function handleHide() {
    startTransition(async () => {
      const result = await hideSearchResult(type, id);
      if (!result.ok) setMessage(result.message);
    });
  }

  return (
    <li className={styles.row}>
      <Link href={href} className={kit.listItem} onClick={() => void recordSearchVisit(type, id, query).catch(() => undefined)}>
        {children}
      </Link>
      {canHide && (
        <button type="button" className={styles.hide} onClick={handleHide} disabled={pending} aria-label="이 검색 결과 숨기기">
          숨기기
        </button>
      )}
      {message && (
        <span role="status" className={kit.faint}>
          {message}
        </span>
      )}
    </li>
  );
}
