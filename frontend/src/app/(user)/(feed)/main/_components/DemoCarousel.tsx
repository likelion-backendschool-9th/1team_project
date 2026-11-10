"use client";

import { useEffect, useRef, useState, type ReactNode } from "react";
import { ChevronRightIcon } from "@/app/_components/ui/Icons";
import styles from "./HotDemos.module.css";

const INTERVAL = 3000;
const RESUME_DELAY = 2500;
const MOVE_LOCK = 800;

type Props = {
  count: number;
  autoplay: boolean;
  title: ReactNode;
  more: ReactNode;
  children: ReactNode;
};

// 카드 한 칸(카드 폭 + 간격)의 실제 픽셀 값. 반올림하지 않아야 오차가 쌓이지 않는다.
function stepOf(list: HTMLUListElement) {
  const items = list.children;
  if (items.length < 2) return 0;
  return items[1].getBoundingClientRect().left - items[0].getBoundingClientRect().left;
}

// 복제 구간(원본 마지막 카드 다음)에 들어가면 같은 모양의 원래 위치로 순간 이동한다.
// 패딩 때문에 최대 스크롤 값이 "한 칸 × 원본 수"보다 조금 작으므로 반 칸 여유를 둔다.
function wrapOf(list: HTMLUListElement, count: number) {
  const size = stepOf(list);
  if (size && list.scrollLeft >= size * count - size / 2) {
    list.scrollTo({ left: Math.max(0, list.scrollLeft - size * count), behavior: "instant" });
  }
}

// 카드를 하나씩 자동으로 넘기고, 사용자가 직접 스크롤하거나 올려 두면 멈췄다가 다시 돈다.
export function DemoCarousel({ count, autoplay, title, more, children }: Props) {
  const listRef = useRef<HTMLUListElement>(null);
  const [paused, setPaused] = useState(false);
  const holdRef = useRef<() => void>(() => {});
  const lockRef = useRef(false);
  const trackRef = useRef<HTMLDivElement>(null);
  const thumbRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const list = listRef.current;
    if (!list || !autoplay || paused) return;
    if (window.matchMedia("(prefers-reduced-motion: reduce)").matches) return;

    let interacting = false;
    let resumeTimer = 0;

    const step = () => stepOf(list);
    const wrap = () => {
      if (!lockRef.current) wrapOf(list, count);
    };

    function hold() {
      interacting = true;
      window.clearTimeout(resumeTimer);
      resumeTimer = window.setTimeout(() => {
        interacting = false;
        wrap();
      }, RESUME_DELAY);
    }
    holdRef.current = hold;

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

  // 기본 스크롤바는 순환용 복제 카드까지 길이로 잡혀서 실제 카드 수와 맞지 않는다.
  // 원본 카드 수를 기준으로 한 막대를 따로 그린다.
  useEffect(() => {
    const list = listRef.current;
    const track = trackRef.current;
    const thumb = thumbRef.current;
    if (!list || !track || !thumb) return;

    function update() {
      const size = stepOf(list!);
      if (!size) return;
      const total = size * count;
      const visible = Math.min(1, list!.clientWidth / total);
      const start = Math.min((list!.scrollLeft % total) / total, 1 - visible);
      track!.hidden = visible >= 0.999;
      thumb!.style.width = `${visible * 100}%`;
      thumb!.style.left = `${start * 100}%`;
    }

    update();
    list.addEventListener("scroll", update, { passive: true });
    window.addEventListener("resize", update);
    return () => {
      list.removeEventListener("scroll", update);
      window.removeEventListener("resize", update);
    };
  }, [count]);

  // 버튼으로 한 칸씩 넘긴다. 자동 넘김은 잠시 멈추고, 처음에서 이전을 누르면 끝으로 이어진다.
  function move(direction: 1 | -1) {
    const list = listRef.current;
    if (!list) return;
    holdRef.current();
    const size = stepOf(list);
    if (!size) return;
    if (direction === 1) wrapOf(list, count);
    else if (autoplay && list.scrollLeft < size / 2) {
      // 이동하는 동안에는 순환 보정이 끼어들어 제자리로 되돌리지 않게 잠근다.
      lockRef.current = true;
      window.setTimeout(() => (lockRef.current = false), MOVE_LOCK);
      list.scrollTo({ left: size * count, behavior: "instant" });
    }
    list.scrollBy({ left: size * direction, behavior: "smooth" });
  }

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
      <div className={styles.viewport}>
        <button
          type="button"
          className={`${styles.arrow} ${styles.prev}`}
          aria-label="이전 데모"
          onClick={() => move(-1)}
        >
          <ChevronRightIcon />
        </button>
        <ul ref={listRef} className={styles.grid} aria-roledescription="캐러셀">
          {children}
        </ul>
        <div ref={trackRef} className={styles.track} aria-hidden="true">
          <div ref={thumbRef} className={styles.trackThumb} />
        </div>
        <button
          type="button"
          className={`${styles.arrow} ${styles.next}`}
          aria-label="다음 데모"
          onClick={() => move(1)}
        >
          <ChevronRightIcon />
        </button>
      </div>
    </>
  );
}
