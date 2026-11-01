"use client";

import { useEffect, useRef } from "react";
import { SearchIcon } from "@/app/_components/ui/Icons";
import styles from "./SearchBar.module.css";

export function SearchBar() {
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    function handleKeyDown(event: KeyboardEvent) {
      if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === "k") {
        event.preventDefault();
        inputRef.current?.focus();
      }
    }
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, []);

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
        className={styles.input}
      />
      <kbd className={styles.shortcut} aria-hidden="true">
        ⌘ K
      </kbd>
    </form>
  );
}
