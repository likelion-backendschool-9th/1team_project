"use client";

import { useState, useTransition } from "react";
import { BookmarkIcon, HeartIcon } from "@/app/_components/ui/Icons";
import kit from "@/app/_components/ui/kit.module.css";
import { formatCount } from "@/app/_lib/format";
import { DemoThumb } from "../../../_components/DemoThumb";
import { playDemo, toggleDemoLike } from "../_actions";
import styles from "./DemoDetail.module.css";

type Props = {
  demoId: number;
  title: string;
  seed: number;
  label: string;
  initialPlays: number;
  initialLikes: number;
  liked: boolean;
};

// 샌드박스 서버가 연결되기 전까지는 iframe에 안내 화면을 띄운다.
const PLACEHOLDER = `<!doctype html><html><body style="margin:0;height:100vh;display:grid;place-items:center;background:#1f1b17;color:#ffd04d;font-family:system-ui">
<div style="text-align:center"><div style="font-size:42px">🥢</div><p>샌드박스 연결 전 미리보기예요</p></div></body></html>`;

export function DemoStage({ demoId, title, seed, label, initialPlays, initialLikes, liked }: Props) {
  const [playing, setPlaying] = useState(false);
  const [plays, setPlays] = useState(initialPlays);
  const [like, setLike] = useState({ active: liked, count: initialLikes });
  const [bookmarked, setBookmarked] = useState(false);
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
          <iframe
            title={`${title} 샌드박스`}
            srcDoc={PLACEHOLDER}
            sandbox="allow-scripts"
            className={styles.frame}
          />
        ) : (
          <DemoThumb seed={seed} thumbnail={null} label={label} className={styles.thumb} />
        )}
      </div>
      <div className={styles.actions}>
        <button type="button" className={kit.btnPrimary} onClick={handlePlay} disabled={playing}>
          {playing ? "실행 중" : "▶ 찍먹하기"}
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
          onClick={() => setBookmarked((value) => !value)}
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
