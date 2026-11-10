"use client";

import { type KeyboardEvent, useEffect, useId, useRef, useState } from "react";
import { Avatar } from "@/app/_components/ui/Avatar";
import { SearchIcon } from "@/app/_components/ui/Icons";
import type { DmCandidateRelation, DmRoom } from "@/app/_lib/types/dm";
import type { UserSummary } from "@/app/_lib/types/user";
import { useDmCandidates } from "../_hooks/useDmCandidates";
import styles from "./Dm.module.css";

type Props = {
  rooms: DmRoom[] | null;
  myId: number | null;
  onSelect: (user: UserSummary) => void;
};

type Option = { user: UserSummary; label: string | null };

const RELATION_LABEL: Record<Exclude<DmCandidateRelation, null>, string> = {
  mutual: "맞팔",
  following: "팔로잉",
  follower: "팔로워",
};

/**
 * DM 창의 사람 찾기. 입력창을 누르면 아래에 작은 후보 목록이 열리고, 스크롤하면 다음 후보를 이어 받는다 (WAI-ARIA combobox 패턴).
 * 순서: 최근에 대화한 사람 → 팔로잉 → 팔로워 → 그 밖의 회원 검색 결과.
 */
export function DmUserSearch({ rooms, myId, onSelect }: Props) {
  const listId = useId();
  const inputRef = useRef<HTMLInputElement>(null);
  const dropdownRef = useRef<HTMLDivElement>(null);
  const moreRef = useRef<HTMLButtonElement>(null);
  const [query, setQuery] = useState("");
  const [open, setOpen] = useState(false);
  const [activeIndex, setActiveIndex] = useState(-1);
  const { users, pending, hasMore, loading, error, loadMore } = useDmCandidates(query, open);

  // 대화방 목록은 이미 받아 왔으니 바로 거르고, 서버 후보는 그 뒤에 겹치지 않게 붙인다.
  const keyword = query.trim().toLowerCase();
  const seen = new Set<number>(myId === null ? [] : [myId]);
  const options: Option[] = [];
  for (const room of rooms ?? []) {
    if (seen.has(room.user.id) || !room.user.nickname.toLowerCase().includes(keyword)) continue;
    seen.add(room.user.id);
    options.push({ user: room.user, label: "최근 대화" });
  }
  for (const { relation, ...user } of users) {
    if (seen.has(user.id)) continue;
    seen.add(user.id);
    options.push({ user, label: relation ? RELATION_LABEL[relation] : null });
  }
  // 후보가 줄어들어 선택 위치가 목록 밖이면 선택하지 않은 것으로 본다
  const active = activeIndex < options.length ? activeIndex : -1;

  // 키보드로 고른 후보가 작은 목록 밖에 있으면 보이게 스크롤한다.
  useEffect(() => {
    if (active < 0) return;
    document.getElementById(`${listId}-${active}`)?.scrollIntoView({ block: "nearest" });
  }, [active, listId]);

  // "더 보기" 버튼이 목록 안에 보이면 자동으로 누른다. 실패하면 멈추고 버튼으로 다시 시도한다.
  useEffect(() => {
    const button = moreRef.current;
    if (!open || !button || !hasMore || error) return;
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) loadMore();
      },
      { root: dropdownRef.current, rootMargin: "80px 0px" },
    );
    observer.observe(button);
    return () => observer.disconnect();
  }, [open, hasMore, error, loadMore]);

  function close() {
    setOpen(false);
    setActiveIndex(-1);
  }

  function handleSelect(user: UserSummary) {
    setQuery("");
    close();
    inputRef.current?.blur();
    onSelect(user);
  }

  function handleKeyDown(event: KeyboardEvent<HTMLInputElement>) {
    // 한글 조합 중에 누른 Enter·화살표는 조합을 끝내는 키라서 무시한다
    if (event.nativeEvent.isComposing) return;
    if (event.key === "ArrowDown" || event.key === "ArrowUp") {
      if (options.length === 0) return;
      event.preventDefault();
      setOpen(true);
      // 입력창(-1) → 첫 후보 → … → 마지막 후보 → 입력창 순서로 돈다
      const step = event.key === "ArrowDown" ? 1 : -1;
      const positions = options.length + 1;
      setActiveIndex(((active + 1 + step + positions) % positions) - 1);
    } else if (event.key === "Enter" && options.length > 0) {
      // 고르지 않고 Enter를 누르면 맨 위 후보와 대화한다
      event.preventDefault();
      handleSelect(options[Math.max(active, 0)].user);
    } else if (event.key === "Escape" && open) {
      // 후보 목록만 닫고 DM 창은 그대로 둔다
      event.preventDefault();
      event.stopPropagation();
      close();
    }
  }

  const status = error
    ? "목록을 불러오지 못했어요."
    : loading || pending
      ? "찾는 중…"
      : options.length === 0
        ? keyword
          ? `'${query.trim()}'와(과) 일치하는 사람이 없어요.`
          : "팔로우한 사람이 없어요. 이름으로 찾아보세요."
        : "";

  return (
    <div className={styles.search}>
      <label className={styles.searchField}>
        <SearchIcon className={styles.searchIcon} />
        <span className="sr-only">DM 보낼 사람 찾기</span>
        <input
          ref={inputRef}
          type="search"
          placeholder="이름으로 사람 찾기"
          autoComplete="off"
          maxLength={50}
          role="combobox"
          aria-autocomplete="list"
          aria-expanded={open}
          aria-controls={listId}
          aria-activedescendant={open && active >= 0 ? `${listId}-${active}` : undefined}
          value={query}
          onChange={(event) => {
            setQuery(event.target.value);
            setOpen(true);
            setActiveIndex(-1);
          }}
          onKeyDown={handleKeyDown}
          onFocus={() => setOpen(true)}
          onBlur={close}
          className={styles.searchInput}
        />
      </label>
      <div
        ref={dropdownRef}
        className={styles.searchDropdown}
        hidden={!open}
        aria-busy={loading}
        // 목록·스크롤바를 누르는 순간 입력창의 blur로 목록이 닫히지 않게 한다
        onMouseDown={(event) => event.preventDefault()}
      >
        <ul id={listId} role="listbox" aria-label="DM 보낼 사람">
          {options.map((option, index) => (
            // 키보드는 입력창의 aria-activedescendant로 다루므로, 후보에는 마우스 선택만 붙인다
            <li
              key={option.user.id}
              id={`${listId}-${index}`}
              role="option"
              aria-selected={index === active}
              className={`${styles.searchOption} ${index === active ? styles.searchOptionActive : ""}`}
              onClick={() => handleSelect(option.user)}
            >
              <Avatar name={option.user.nickname} size={28} />
              <span className={styles.searchName}>{option.user.nickname}</span>
              {option.label && <span className={styles.searchTag}>{option.label}</span>}
            </li>
          ))}
        </ul>
        <p role="status" className={styles.searchStatus}>
          {open ? status : ""}
        </p>
        {hasMore && (
          <button ref={moreRef} type="button" className={styles.searchMore} onClick={loadMore} disabled={loading}>
            {error ? "다시 시도" : "더 보기"}
          </button>
        )}
      </div>
    </div>
  );
}
