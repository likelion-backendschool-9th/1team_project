"use client";

import { useRouter } from "next/navigation";
import { useEffect, useRef, useState, useTransition } from "react";
import kit from "@/app/_components/ui/kit.module.css";
import { followsHref, MAX_FOLLOW_QUERY, type FollowTab } from "../_lib/params";
import list from "./FollowList.module.css";

const DEBOUNCE_MS = 300;

type Props = {
  tab: FollowTab;
  q: string;
};

// 입력을 잠깐 멈추면 주소의 ?q=를 바꿔서 서버가 검색 결과 첫 페이지를 다시 그린다.
export function FollowSearch({ tab, q }: Props) {
  const router = useRouter();
  const [value, setValue] = useState(q);
  const [syncedQ, setSyncedQ] = useState(q);
  // 뒤로 가기 등으로 주소의 q가 바뀌면 입력 칸도 맞춘다 (렌더 중에 맞춰서 effect를 거치지 않는다).
  // 주소에는 앞뒤 공백을 뺀 값이 들어가서, 입력 중인 값과 같은 검색어면 그대로 둔다 ("시드 " 입력 중 공백이 지워지지 않게).
  if (q !== syncedQ) {
    setSyncedQ(q);
    if (q !== value.trim()) setValue(q);
  }
  const [pending, startTransition] = useTransition();
  const timer = useRef<number | undefined>(undefined);

  useEffect(() => () => window.clearTimeout(timer.current), []);

  function search(next: string) {
    startTransition(() => router.replace(followsHref(tab, next.trim()), { scroll: false }));
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
