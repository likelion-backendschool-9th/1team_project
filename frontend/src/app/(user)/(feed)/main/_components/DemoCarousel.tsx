"use client";

import { useEffect, useRef, useState, type ReactNode } from "react";
import styles from "./HotDemos.module.css";

export const VISIBLE_CARDS = 4;
const INTERVAL = 3000;
const RESUME_DELAY = 2500;

type Props = {
  count: number;
  autoplay: boolean;
  title: ReactNode;
  more: ReactNode;
  children: ReactNode;
};

// 카드를 하나씩 자동으로 넘기고, 사용자가 직접 스크롤하거나 올려 두면 멈췄다가 다시 돈다.
export function DemoCarousel({ count, autoplay, title, more, children }: Props) {
  const listRef = useRef<HTMLUListElement>(null);
  const [paused, setPaused] = useState(false);

  useEffect(() => {
    const list = listRef.current;
    if (!list || !autoplay || paused) return;
    if (window.matchMedia("(prefers-reduced-motion: reduce)").matches) return;

    let interacting = false;
    let resumeTimer = 0;

    function step() {
      const items = list!.children;
      if (items.length < 2) return 0;
      return (items[1] as HTMLElement).offsetLeft - (items[0] as HTMLElement).offsetLeft;
    }

    // 복제 구간에 들어가면 같은 모양의 원래 위치로 순간 이동한다.
    function wrap() {
      const size = step();
      if (size && list!.scrollLeft >= size * count - 1) {
        list!.scrollTo({ left: list!.scrollLeft - size * count, behavior: "instant" });
      }
    }

    function hold() {
      interacting = true;
      window.clearTimeout(resumeTimer);
      resumeTimer = window.setTimeout(() => {
        interacting = false;
        wrap();
      }, RESUME_DELAY);
    }

    const timer = window.setInterval(() => {
      if (interacting || document.hidden) return;
      wrap();
      list.scrollBy({ left: step(), behavior: "smooth" });
    }, INTERVAL);

    const holdEvents = ["wheel", "touchstart", "pointerdown", "keydown"] as const;
    holdEvents.forEach((name) => list.addEventListener(name, hold, { passive: true }));
    list.addEventListener("pointerenter", hold);
    list.addEventListener("pointermove", hold, { passive: true });
    list.addEventListener("pointerleave", hold);
    list.addEventListener("focusin", hold);
    list.addEventListener("scrollend", wrap);

    return () => {
      window.clearInterval(timer);
      window.clearTimeout(resumeTimer);
      holdEvents.forEach((name) => list.removeEventListener(name, hold));
      list.removeEventListener("pointerenter", hold);
      list.removeEventListener("pointermove", hold);
      list.removeEventListener("pointerleave", hold);
      list.removeEventListener("focusin", hold);
      list.removeEventListener("scrollend", wrap);
    };
  }, [autoplay, count, paused]);

  return (
    <>
      <div className={styles.header}>
        {title}
        <div className={styles.controls}>
          {autoplay && (
            <button
              type="button"
              className={styles.pause}
              aria-pressed={paused}
              aria-label={paused ? "자동 넘김 재생" : "자동 넘김 일시정지"}
              onClick={() => setPaused((value) => !value)}
            >
              {paused ? "▶" : "❚❚"}
            </button>
          )}
          {more}
        </div>
      </div>
      <ul ref={listRef} className={styles.grid} aria-roledescription="캐러셀">
        {children}
      </ul>
    </>
  );
}
