"use client";

import { useState, useTransition } from "react";
import { BookmarkIcon, HeartIcon } from "@/app/_components/ui/Icons";
import kit from "@/app/_components/ui/kit.module.css";
import { formatCount } from "@/app/_lib/format";
import { DemoThumb } from "@/app/(user)/(feed)/_components/DemoThumb";
import { playDemo, toggleDemoBookmark, toggleDemoLike } from "../_actions";
import styles from "./DemoDetail.module.css";

type Props = {
  demoId: number;
  title: string;
  seed: number;
  label: string;
  thumbnail: string | null;
  /** 실행 주소. 공개 전이거나 목 모드면 없다. */
  runUrl: string | null;
  initialPlays: number;
  initialLikes: number;
  liked: boolean;
  bookmarked: boolean;
};

export function DemoStage({
  demoId,
  title,
  seed,
  label,
  thumbnail,
  runUrl,
  initialPlays,
  initialLikes,
  liked,
  bookmarked: initialBookmarked,
}: Props) {
  const [playing, setPlaying] = useState(false);
  const [plays, setPlays] = useState(initialPlays);
  const [like, setLike] = useState({ active: liked, count: initialLikes });
  const [bookmarked, setBookmarked] = useState(initialBookmarked);
  const [message, setMessage] = useState<string | null>(null);
  const [pending, startTransition] = useTransition();

  function handlePlay() {
    setPlaying(true);
    setPlays((value) => value + 1);
    startTransition(async () => {
      const result = await playDemo(demoId);
      if (!result.ok) setMessage(result.message);
    });
  }

  function handleLike() {
    const previous = like;
    const next = { active: !like.active, count: like.count + (like.active ? -1 : 1) };
    setLike(next);
    startTransition(async () => {
      const result = await toggleDemoLike(demoId, next.active);
      if (!result.ok) {
        setLike(previous);
        setMessage(result.message);
      }
    });
  }

  return (
    <div className={styles.stageWrap}>
      <div className={styles.stage}>
        {playing ? (
          // allow-same-origin을 넣지 않는다. 데모는 opaque origin에서 돌아 찍먹 쿠키·저장소에 닿지 못한다
          <iframe
            title={`${title} 샌드박스`}
            src={runUrl ?? undefined}
            sandbox="allow-scripts"
            referrerPolicy="no-referrer"
            className={styles.frame}
          />
        ) : (
          <DemoThumb seed={seed} thumbnail={thumbnail} label={label} className={styles.thumb} />
        )}
      </div>
      <div className={styles.actions}>
        <button type="button" className={kit.btnPrimary} onClick={handlePlay} disabled={playing || !runUrl}>
          {playing ? "실행 중" : runUrl ? "▶ 찍먹하기" : "아직 실행할 수 없어요"}
        </button>
        <button
          type="button"
          className={`${kit.btnGhost} ${like.active ? styles.liked : ""}`}
          aria-pressed={like.active}
          disabled={pending}
          onClick={handleLike}
        >
          <HeartIcon filled={like.active} />
          <span className="sr-only">좋아요</span>
          {formatCount(like.count)}
        </button>
        <button
          type="button"
          className={`${kit.btnGhost} ${bookmarked ? styles.saved : ""}`}
          aria-pressed={bookmarked}
          onClick={() => {
            const next = !bookmarked;
            setBookmarked(next);
            startTransition(async () => {
              const result = await toggleDemoBookmark(demoId, next);
              if (!result.ok) {
                setBookmarked(!next);
                setMessage(result.message);
              }
            });
          }}
        >
          <BookmarkIcon filled={bookmarked} />
          나중에 해보기
        </button>
        <span className={styles.plays}>체험 {formatCount(plays)}회</span>
      </div>
      <p role="alert" className={kit.error}>
        {message}
      </p>
    </div>
  );
}
