"use client";

import Link from "next/link";
import { useCallback, useEffect, useRef, useState } from "react";
import { Avatar } from "@/app/_components/ui/Avatar";
import kit from "@/app/_components/ui/kit.module.css";
import { DmOpenButton } from "@/app/(user)/_components/DmOpenButton";
import { FollowButton } from "../../../_components/FollowButton";
import type { FollowPage } from "../../_lib/api";
import styles from "../../_components/My.module.css";
import { loadFollowPage } from "../_actions";
import type { FollowTab } from "../_lib/params";
import list from "./FollowList.module.css";

type Props = {
  tab: FollowTab;
  q: string;
  initial: FollowPage;
};

// 첫 페이지는 서버에서 받아 오고, 맨 아래에 닿으면 다음 페이지를 이어 붙인다.
export function FollowList({ tab, q, initial }: Props) {
  const [users, setUsers] = useState(initial.users);
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
      const next = await loadFollowPage(tab, page + 1, q);
      setUsers((prev) => {
        // 그 사이 팔로우가 바뀌어 같은 사람이 다시 와도 한 번만 보인다.
        const seen = new Set(prev.map((user) => user.id));
        return [...prev, ...next.users.filter((user) => !seen.has(user.id))];
      });
      setPage(next.page);
    } catch {
      setError(true);
    } finally {
      loadingRef.current = false;
      setLoading(false);
    }
  }, [tab, page, q]);

  // "더 보기" 버튼이 화면에 보이면 자동으로 누른다. 키보드 사용자는 버튼을 직접 누를 수 있다.
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

  if (users.length === 0) {
    return (
      <section className={kit.card} aria-label={tab === "followers" ? "팔로워" : "팔로잉"}>
        <p className={kit.empty}>{q ? `'${q}'와(과) 일치하는 사람이 없어요.` : "아직 아무도 없어요."}</p>
      </section>
    );
  }

  return (
    <section className={kit.card} aria-label={tab === "followers" ? "팔로워" : "팔로잉"} aria-busy={loading}>
      <ul className={kit.list}>
        {users.map((user) => (
          <li key={user.id} className={kit.listItem}>
            <Link href={`/mini-home/${user.id}`} className={styles.person}>
              <Avatar name={user.nickname} size={44} />
              <span className={kit.grow}>
                <strong>{user.nickname}</strong>
                {user.followsMe && tab === "followings" && (
                  <span className={kit.pill} style={{ marginLeft: 6 }}>
                    맞팔
                  </span>
                )}
                <span className={kit.faint} style={{ display: "block" }}>
                  {user.techStacks.join(", ") || "기술 스택 미입력"}
                </span>
              </span>
            </Link>
            <DmOpenButton user={user} className={`${kit.btnGhost} ${kit.sm}`}>
              DM
            </DmOpenButton>
            <FollowButton userId={user.id} nickname={user.nickname} following={user.following} />
          </li>
        ))}
      </ul>
      <div className={list.footer}>
        <p role="status" className={kit.faint}>
          {loading ? "불러오는 중…" : error ? "목록을 불러오지 못했어요." : hasMore ? "" : `${users.length}명을 모두 불러왔어요.`}
        </p>
        {hasMore && (
          <button ref={moreRef} type="button" className={`${kit.btnGhost} ${kit.sm}`} onClick={loadMore} disabled={loading}>
            {error ? "다시 시도" : "더 보기"}
          </button>
        )}
      </div>
    </section>
  );
}
