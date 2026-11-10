"use client";

import { useEffect, useRef, useState, type ReactNode } from "react";
import { playDemo } from "../demos/[demoId]/_actions";
import { createNonce, initialFrameStatus, nextFrameStatus, parseDemoMessage } from "../demos/[demoId]/_lib/demoFrame";
import styles from "./FeaturedDemo.module.css";

type Props = {
  demoId: number;
  title: string;
  /** 실행 주소. 공개 전이거나 목 모드면 없다 */
  runUrl: string | null;
  /** 실행 전 보여 줄 그림(썸네일 등) */
  poster: ReactNode;
};

// 데모 상세의 DemoStage와 같은 방식(샌드박스 iframe + 상태 신호)으로 미니홈피 메인에서 바로 실행한다.
const LOAD_TIMEOUT_MS = 20_000;

export function FeaturedRunner({ demoId, title, runUrl, poster }: Props) {
  const [run, setRun] = useState<{ nonce: string } | null>(null);
  const [frame, setFrame] = useState(initialFrameStatus);
  const frameRef = useRef<HTMLIFrameElement>(null);

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

  function start(countPlay: boolean) {
    setFrame(initialFrameStatus);
    setRun({ nonce: createNonce() });
    // 체험 횟수는 실패해도 실행을 막지 않는다
    if (countPlay) void playDemo(demoId);
  }

  if (!run) {
    return (
      <>
        {poster}
        {runUrl ? (
          <button type="button" className={styles.playButton} onClick={() => start(true)}>
            ▶ 여기서 바로 실행
          </button>
        ) : (
          <span className={styles.playDisabled}>아직 실행할 수 없는 데모예요</span>
        )}
      </>
    );
  }

  return (
    <>
      {/* allow-same-origin을 넣지 않는다. 데모는 opaque origin에서 돌아 찍먹 쿠키·저장소에 닿지 못한다 */}
      <iframe
        key={run.nonce}
        ref={frameRef}
        title={`${title} 샌드박스`}
        src={runUrl ? `${runUrl}?dip_nonce=${run.nonce}` : undefined}
        sandbox="allow-scripts"
        referrerPolicy="no-referrer"
        className={styles.frame}
      />
      {frame.phase === "loading" && (
        <p className={styles.frameOverlay} role="status" aria-busy="true">
          데모를 불러오는 중…
        </p>
      )}
      {frame.phase === "failed" && (
        <div className={styles.frameOverlay} role="alert">
          <strong>데모를 실행하지 못했어요</strong>
          <span>{frame.reason}</span>
          <button type="button" className={styles.playButtonInline} onClick={() => start(false)}>
            다시 시도
          </button>
        </div>
      )}
    </>
  );
}
