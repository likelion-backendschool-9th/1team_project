"use client";

import { useEffect, useRef, useState } from "react";
import { BGM_TRACKS } from "../_lib/bgm";
import styles from "./ProfilePanel.module.css";

type Props = {
  trackId: string;
};

// 구경하다 갑자기 크게 나오지 않도록 작게 시작한다. 더 크게 듣고 싶으면 기기 볼륨으로 올린다.
const DEFAULT_VOLUME = 0.3;

// 딥소스 플러스 전용 미니홈피 BGM. 자동 재생하지 않고 사용자가 눌렀을 때만 튼다.
export function BgmPlayer({ trackId }: Props) {
  const audioRef = useRef<HTMLAudioElement>(null);
  const [index, setIndex] = useState(() => Math.max(0, BGM_TRACKS.findIndex((track) => track.id === trackId)));
  const [playing, setPlaying] = useState(false);
  const [missing, setMissing] = useState(false);
  const track = BGM_TRACKS[index];

  // 곡을 바꾸면 audio 요소가 새로 만들어지므로 그때마다 볼륨을 다시 맞춘다.
  useEffect(() => {
    if (audioRef.current) audioRef.current.volume = DEFAULT_VOLUME;
  }, [track.id]);

  async function toggle() {
    const audio = audioRef.current;
    if (!audio) return;
    if (playing) {
      audio.pause();
      return;
    }
    try {
      await audio.play();
      setMissing(false);
    } catch {
      setMissing(true);
    }
  }

  function next() {
    setIndex((value) => (value + 1) % BGM_TRACKS.length);
    setPlaying(false);
    setMissing(false);
  }

  return (
    <div className={styles.bgm}>
      <span className={styles.note} aria-hidden="true">
        ♬
      </span>
      <span className={styles.bgmTitle}>
        <span className="sr-only">배경음악 </span>
        {track.title}
        {missing && <span className={styles.bgmMissing}> · 음원 준비 중</span>}
      </span>
      <button
        type="button"
        className={styles.bgmButton}
        aria-label={playing ? "배경음악 일시정지" : "배경음악 재생"}
        onClick={toggle}
      >
        {playing ? "❚❚" : "▶"}
      </button>
      <button type="button" className={styles.bgmButton} aria-label="다음 곡" onClick={next}>
        ⏭
      </button>
      <audio
        key={track.id}
        ref={audioRef}
        src={track.src}
        preload="none"
        loop
        onPlay={() => setPlaying(true)}
        onPause={() => setPlaying(false)}
        onError={() => {
          setPlaying(false);
          setMissing(true);
        }}
      />
    </div>
  );
}
