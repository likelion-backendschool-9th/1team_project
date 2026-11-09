"use client";

import { useState } from "react";
import kit from "@/app/_components/ui/kit.module.css";
import { createServerPreview } from "../../_lib/uploadZip";
import { buildSrcdoc, planPreview, readZip } from "../_lib/localPreview";

type View =
  | { kind: "idle" }
  | { kind: "busy" }
  | { kind: "local"; srcdoc: string }
  | { kind: "needsServer"; reason: string }
  | { kind: "server"; url: string; expiresAt: string }
  | { kind: "error"; message: string };

type Props = {
  file: File;
};

const frameStyle = { width: "100%", aspectRatio: "16 / 10", border: 0, borderRadius: 12, background: "#fff" };

// 미리보기도 실제 실행과 같은 sandbox로 띄운다. 코드를 찍먹 페이지에 직접 넣지 않는다.
export function ZipPreview({ file }: Props) {
  const [view, setView] = useState<View>({ kind: "idle" });

  async function handleLocal() {
    setView({ kind: "busy" });
    let entries;
    try {
      // 선택한 뒤 원본 파일이 지워지거나 바뀌면 읽기가 실패한다
      entries = await readZip(await file.arrayBuffer());
    } catch {
      setView({ kind: "error", message: "파일을 읽지 못했어요. 다시 선택해 주세요." });
      return;
    }
    if (!entries) {
      setView({ kind: "needsServer", reason: "브라우저에서 이 zip을 읽지 못했어요." });
      return;
    }
    const plan = planPreview(entries);
    setView(
      plan.mode === "local"
        ? { kind: "local", srcdoc: buildSrcdoc(plan.files) }
        : { kind: "needsServer", reason: plan.reason },
    );
  }

  async function handleServer() {
    setView({ kind: "busy" });
    const result = await createServerPreview(file);
    setView(
      result.ok
        ? { kind: "server", url: result.data.url, expiresAt: result.data.expiresAt }
        : { kind: "error", message: result.message },
    );
  }

  return (
    <div className={kit.stack}>
      {view.kind === "idle" && (
        <div>
          <button type="button" className={`${kit.btnGhost} ${kit.sm}`} onClick={handleLocal}>
            공개 전에 미리보기
          </button>
        </div>
      )}
      {view.kind === "busy" && (
        <p role="status" aria-busy="true" className={kit.muted}>
          미리보기를 준비하는 중…
        </p>
      )}
      {view.kind === "local" && (
        <>
          <p role="status" className={kit.faint}>
            브라우저 안에서만 실행하는 미리보기예요. 파일은 서버로 전송되지 않았어요.
          </p>
          <iframe title="데모 로컬 미리보기" srcDoc={view.srcdoc} sandbox="allow-scripts" style={frameStyle} />
        </>
      )}
      {view.kind === "needsServer" && (
        <>
          <p className={kit.muted}>{view.reason}</p>
          <p className={kit.faint}>
            임시 업로드로 미리 볼 수 있어요. 파일이 서버로 전송되어 비공개 경로에 잠시 저장되고, 만료되면 지워져요.
          </p>
          <div>
            <button type="button" className={`${kit.btnGhost} ${kit.sm}`} onClick={handleServer}>
              임시 업로드로 미리보기
            </button>
          </div>
        </>
      )}
      {view.kind === "server" && (
        <>
          <p role="status" className={kit.faint}>
            임시 업로드 미리보기예요. {new Date(view.expiresAt).toLocaleTimeString("ko-KR")}까지만 열려요.
          </p>
          <iframe
            title="데모 임시 미리보기"
            src={view.url}
            sandbox="allow-scripts"
            referrerPolicy="no-referrer"
            style={frameStyle}
          />
        </>
      )}
      {view.kind === "error" && (
        <p role="alert" className={kit.error}>
          {view.message}
        </p>
      )}
    </div>
  );
}
