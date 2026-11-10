"use client";

import { type KeyboardEvent, useEffect, useId, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import { SearchIcon } from "@/app/_components/ui/Icons";
import { loadPopularKeywords } from "../_actions";
import { useAutocomplete } from "../_hooks/useAutocomplete";
import styles from "./SearchBar.module.css";

/** 검색창. 비어 있으면 인기 검색어를, 입력 중이면 자동완성 후보를 보여 준다 (WAI-ARIA combobox 패턴) */
export function SearchBar() {
  const router = useRouter();
  const inputRef = useRef<HTMLInputElement>(null);
  const listId = useId();
  const popularRequest = useRef(false);
  const [query, setQuery] = useState("");
  const [open, setOpen] = useState(false);
  const [activeIndex, setActiveIndex] = useState(-1);
  const [popular, setPopular] = useState<string[]>([]);
  const suggestions = useAutocomplete(query);
  const trimmed = query.trim();
  const showingPopular = open && !trimmed && popular.length > 0;
  const showingSuggestions = open && Boolean(trimmed) && suggestions.length > 0;
  const items = showingPopular ? popular : showingSuggestions ? suggestions.map((item) => item.text) : [];
  const expanded = items.length > 0;
  // 후보가 줄어들어 선택 위치가 목록 밖이면 선택하지 않은 것으로 본다
  const active = expanded && activeIndex < items.length ? activeIndex : -1;

  useEffect(() => {
    function handleKeyDown(event: globalThis.KeyboardEvent) {
      if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === "k") {
        event.preventDefault();
        inputRef.current?.focus();
      }
    }
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, []);

  function loadPopular() {
    if (popularRequest.current) return;
    popularRequest.current = true;
    void loadPopularKeywords().then(setPopular);
  }

  function handleSelect(text: string) {
    setQuery(text);
    setOpen(false);
    setActiveIndex(-1);
    router.push(`/search?${new URLSearchParams({ q: text })}`);
  }

  function handleKeyDown(event: KeyboardEvent<HTMLInputElement>) {
    // 한글 조합 중에 누른 Enter·화살표는 조합을 끝내는 키라서 무시한다. 무시하지 않으면 Enter가 두 번 처리된다
    if (event.nativeEvent.isComposing) return;
    if (event.key === "ArrowDown" || event.key === "ArrowUp") {
      if (items.length === 0) return;
      event.preventDefault();
      setOpen(true);
      // 입력창(-1) → 첫 후보 → … → 마지막 후보 → 입력창 순서로 돈다
      const step = event.key === "ArrowDown" ? 1 : -1;
      const positions = items.length + 1;
      setActiveIndex(((active + 1 + step + positions) % positions) - 1);
    } else if (event.key === "Enter" && active >= 0) {
      // 후보를 고른 상태면 그 후보로 검색한다. 고르지 않았으면 폼이 입력값 그대로 제출된다
      event.preventDefault();
      handleSelect(items[active]);
    } else if (event.key === "Escape" && expanded) {
      event.preventDefault();
      setOpen(false);
      setActiveIndex(-1);
    }
  }

  return (
    <form role="search" action="/search" method="get" className={styles.form}>
      <label htmlFor="search-input" className="sr-only">
        사람, 데모, 글 검색
      </label>
      <SearchIcon className={styles.icon} />
      <input
        ref={inputRef}
        id="search-input"
        name="q"
        type="search"
        placeholder="사람, 데모, 글 검색"
        autoComplete="off"
        maxLength={50}
        role="combobox"
        aria-autocomplete="list"
        aria-expanded={expanded}
        aria-controls={listId}
        aria-activedescendant={active >= 0 ? `${listId}-${active}` : undefined}
        value={query}
        onChange={(event) => {
          setQuery(event.target.value);
          setOpen(true);
          setActiveIndex(-1);
        }}
        onKeyDown={handleKeyDown}
        onFocus={() => {
          setOpen(true);
          loadPopular();
        }}
        onBlur={() => setOpen(false)}
        className={styles.input}
      />
      <kbd className={styles.shortcut} aria-hidden="true">
        ⌘ K
      </kbd>
      <div className={styles.panel} hidden={!expanded}>
        {showingPopular && <p className={styles.heading}>인기 검색어</p>}
        <ul id={listId} role="listbox" aria-label={showingPopular ? "인기 검색어" : "추천 검색어"} className={styles.listbox}>
          {items.map((text, index) => (
            // 키보드는 입력창의 aria-activedescendant로 다루므로, 후보에는 마우스 선택만 붙인다
            <li
              key={`${text}-${index}`}
              id={`${listId}-${index}`}
              role="option"
              aria-selected={index === active}
              className={`${styles.option} ${index === active ? styles.optionActive : ""}`}
              // 누르는 순간 입력창의 blur로 목록이 닫히지 않게 한다
              onMouseDown={(event) => event.preventDefault()}
              onClick={() => handleSelect(text)}
            >
              <span className={styles.optionText}>{text}</span>
              {showingSuggestions && suggestions[index]?.type === "TAG" && <span className={styles.tagBadge}>태그</span>}
              {showingSuggestions && suggestions[index]?.type === "CORRECTION" && <span className={styles.tagBadge}>한/영 보정</span>}
            </li>
          ))}
        </ul>
      </div>
      <p role="status" className="sr-only">
        {showingPopular
          ? `인기 검색어 ${popular.length}개. 위아래 화살표로 고를 수 있어요.`
          : showingSuggestions
            ? `추천 검색어 ${suggestions.length}개. 위아래 화살표로 고를 수 있어요.`
            : ""}
      </p>
    </form>
  );
}
