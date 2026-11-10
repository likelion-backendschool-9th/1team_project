"use client";

import { useEffect, useRef, useState, useTransition } from "react";
import { BookmarkIcon, HeartIcon } from "@/app/_components/ui/Icons";
import kit from "@/app/_components/ui/kit.module.css";
import { ReportModal } from "@/app/_components/ui/ReportModal";
import { formatCount } from "@/app/_lib/format";
import { DemoThumb } from "@/app/(user)/(feed)/_components/DemoThumb";
import { useExpandStage } from "@/app/(user)/_components/useExpandStage";
import { playDemo, reportDemo, toggleDemoBookmark, toggleDemoLike } from "../_actions";
import { createNonce, initialFrameStatus, nextFrameStatus, parseDemoMessage } from "../_lib/demoFrame";
import styles from "./DemoDetail.module.css";

type Props = {
  demoId: number;
  title: string;
  seed: number;
  label: string;
  thumbnail: string | null;
  /** 실행 주소. 공개 전이거나 목 모드면 없다. */
  runUrl: string | null;
  /** 본인 데모면 신고 버튼을 숨긴다. */
  mine: boolean;
  initialPlays: number;
  initialLikes: number;
  liked: boolean;
  bookmarked: boolean;
};

// iframe onLoad는 빈 화면이어도 불리므로 실행 판단에 쓰지 않는다. 데모 안의 상태 스크립트가 신호를 보내는지로 본다
const LOAD_TIMEOUT_MS = 20_000;

export function DemoStage({
  demoId,
  title,
  seed,
  label,
  thumbnail,
  runUrl,
  mine,
  initialPlays,
  initialLikes,
  liked,
  bookmarked: initialBookmarked,
}: Props) {
  const [run, setRun] = useState<{ nonce: string } | null>(null);
  const [frame, setFrame] = useState(initialFrameStatus);
  const frameRef = useRef<HTMLIFrameElement>(null);
  const playing = run !== null;
  const [plays, setPlays] = useState(initialPlays);
  const [like, setLike] = useState({ active: liked, count: initialLikes });
  const [bookmarked, setBookmarked] = useState(initialBookmarked);
  const [message, setMessage] = useState<string | null>(null);
  const [pending, startTransition] = useTransition();
  const { expanded, setExpanded, ref: stageRef } = useExpandStage<HTMLDivElement>();

  useEffect(() => {
    if (!run) return;
    const { nonce } = run;
    function handleMessage(event: MessageEvent) {
      // sandbox iframe의 출처는 "null"이다. 내 iframe이 보낸 것만 받는다
      if (event.source !== frameRef.current?.contentWindow || event.origin !== "null") return;
      const message = parseDemoMessage(event.data, nonce);
      if (message) setFrame((current) => nextFrameStatus(current, message));
    }
    window.addEventListener("message", handleMessage);
    const timer = window.setTimeout(() => {
      setFrame((current) => nextFrameStatus(current, { type: "timeout" }));
    }, LOAD_TIMEOUT_MS);
    return () => {
      window.removeEventListener("message", handleMessage);
      window.clearTimeout(timer);
    };
  }, [run]);

  function startFrame() {
    setFrame(initialFrameStatus);
    setRun({ nonce: createNonce() });
  }

  function handlePlay() {
    startFrame();
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
    <div ref={stageRef} className={`${styles.stageWrap} ${expanded ? styles.expanded : ""}`}>
      <div className={styles.stage}>
        {run ? (
          // allow-same-origin을 넣지 않는다. 데모는 opaque origin에서 돌아 찍먹 쿠키·저장소에 닿지 못한다
          <iframe
            key={run.nonce}
            ref={frameRef}
            title={`${title} 샌드박스`}
            src={runUrl ? `${runUrl}?dip_nonce=${run.nonce}` : undefined}
            sandbox="allow-scripts"
            referrerPolicy="no-referrer"
            className={styles.frame}
          />
        ) : (
          <DemoThumb seed={seed} thumbnail={thumbnail} label={label} className={styles.thumb} />
        )}
        {run && frame.phase === "loading" && (
          <p className={styles.overlay} role="status" aria-busy="true">
            데모를 불러오는 중…
          </p>
        )}
        {run && frame.phase === "failed" && (
          <div className={styles.overlay} role="alert">
            <p className={styles.overlayTitle}>데모를 실행하지 못했어요</p>
            <p>{frame.reason}</p>
            <div className={kit.row}>
              <button type="button" className={`${kit.btnPrimary} ${kit.sm}`} onClick={startFrame}>
                다시 시도
              </button>
              {!mine && (
                <ReportModal
                  label="문제 신고"
                  title="이 데모의 문제를 신고할까요?"
                  className={`${kit.btnGhost} ${kit.sm}`}
                  onConfirm={(reason) => reportDemo(demoId, reason)}
                />
              )}
            </div>
          </div>
        )}
      </div>
      {run && frame.phase !== "failed" && frame.warning && (
        <p role="status" className={kit.faint}>
          {frame.warning}
        </p>
      )}
      <div className={styles.actions}>
        <button type="button" className={kit.btnPrimary} onClick={handlePlay} disabled={playing || !runUrl}>
          {playing ? "실행 중" : runUrl ? "▶ 찍먹하기" : "아직 실행할 수 없어요"}
        </button>
        <button
          type="button"
          className={styles.run}
          aria-pressed={expanded}
          onClick={() => setExpanded((value) => !value)}
        >
          {expanded ? "↙ 작은 화면으로" : "▸ 큰 화면으로 실행"}
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
