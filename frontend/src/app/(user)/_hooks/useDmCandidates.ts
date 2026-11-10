import { useCallback, useEffect, useRef, useState } from "react";
import type { DmCandidate, DmCandidateCursor } from "@/app/_lib/types/dm";
import { loadDmCandidates } from "../_actions";

/** 입력을 멈추고 이만큼 지나면 후보를 요청한다. 빠르게 치는 동안 키마다 요청하지 않는다 */
const DEBOUNCE_MS = 250;

const FIRST_PAGE: DmCandidateCursor = { stage: "followings", page: 1 };

type Loaded = {
  query: string;
  users: DmCandidate[];
  next: DmCandidateCursor | null;
  error: boolean;
};

/**
 * DM 보낼 사람 후보(팔로잉 → 팔로워 → 전체 회원 검색). enabled일 때만 받아 오고, loadMore로 다음 페이지를 이어 붙인다.
 * 검색어가 바뀌면 처음부터 다시 받고, 늦게 도착한 이전 검색어의 응답은 버린다.
 */
export function useDmCandidates(query: string, enabled: boolean) {
  const trimmed = query.trim();
  const [loaded, setLoaded] = useState<Loaded | null>(null);
  const [loading, setLoading] = useState(false);
  const latestRequest = useRef(0);
  const loadingRef = useRef(false);

  const fetchPage = useCallback(async (q: string, cursor: DmCandidateCursor, previous: DmCandidate[]) => {
    const request = ++latestRequest.current;
    loadingRef.current = true;
    setLoading(true);
    try {
      const page = await loadDmCandidates(q, cursor);
      if (request === latestRequest.current) setLoaded({ query: q, users: [...previous, ...page.users], next: page.next, error: false });
    } catch {
      // 다시 시도하면 실패한 페이지부터 받는다.
      if (request === latestRequest.current) setLoaded({ query: q, users: previous, next: cursor, error: true });
    } finally {
      if (request === latestRequest.current) {
        loadingRef.current = false;
        setLoading(false);
      }
    }
  }, []);

  const loadedQuery = loaded?.query;
  useEffect(() => {
    if (!enabled || loadedQuery === trimmed) return;
    // 검색어를 지웠을 때는 기다리지 않고 바로 팔로우 목록을 보여 준다.
    const timer = setTimeout(() => void fetchPage(trimmed, FIRST_PAGE, []), trimmed ? DEBOUNCE_MS : 0);
    return () => clearTimeout(timer);
  }, [enabled, trimmed, loadedQuery, fetchPage]);

  const current = loaded?.query === trimmed ? loaded : null;

  const loadMore = useCallback(() => {
    if (!current?.next || loadingRef.current) return;
    void fetchPage(current.query, current.next, current.users);
  }, [current, fetchPage]);

  return {
    users: current?.users ?? [],
    /** 지금 검색어의 첫 페이지를 아직 받지 못했는지 */
    pending: !current,
    hasMore: Boolean(current?.next),
    loading,
    error: current?.error ?? false,
    loadMore,
  };
}
