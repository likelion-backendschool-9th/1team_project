"use client";

import { usePathname, useRouter } from "next/navigation";
import { useEffect, useRef, useState, useTransition } from "react";
import kit from "@/app/_components/ui/kit.module.css";
import { MAX_FOLLOW_QUERY, type FollowTab } from "../_lib/params";
import list from "./FollowList.module.css";

const DEBOUNCE_MS = 300;

type Props = {
  tab: FollowTab;
  q: string;
};

// 입력을 잠깐 멈추면 주소의 ?q=를 바꿔서 서버가 검색 결과 첫 페이지를 다시 그린다.
export function FollowSearch({ tab, q }: Props) {
  const router = useRouter();
  const pathname = usePathname();
  const [value, setValue] = useState(q);
  const [pending, startTransition] = useTransition();
  const timer = useRef<number | undefined>(undefined);

  useEffect(() => () => window.clearTimeout(timer.current), []);

  function search(next: string) {
    const params = new URLSearchParams();
    if (tab === "followers") params.set("tab", "followers");
    if (next.trim()) params.set("q", next.trim());
    const query = params.toString();
    startTransition(() => router.replace(query ? `${pathname}?${query}` : pathname, { scroll: false }));
  }

  return (
    <form
      role="search"
      className={list.search}
      onSubmit={(event) => {
        event.preventDefault();
        window.clearTimeout(timer.current);
        search(value);
      }}
    >
      <label htmlFor="follow-search" className="sr-only">
        {tab === "followers" ? "팔로워" : "팔로잉"} 닉네임 검색
      </label>
      <span className={list.searchIcon} aria-hidden="true">
        🔍
      </span>
      <input
        id="follow-search"
        type="search"
        value={value}
        maxLength={MAX_FOLLOW_QUERY}
        placeholder="닉네임으로 찾기"
        autoComplete="off"
        className={kit.input}
        aria-busy={pending}
        onChange={(event) => {
          const next = event.target.value;
          setValue(next);
          window.clearTimeout(timer.current);
          timer.current = window.setTimeout(() => search(next), DEBOUNCE_MS);
        }}
      />
    </form>
  );
}
