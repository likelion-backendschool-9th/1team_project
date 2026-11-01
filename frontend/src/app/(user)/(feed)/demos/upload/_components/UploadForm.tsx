"use client";

import { useActionState, useEffect, useState } from "react";
import { FormMessage } from "@/app/_components/ui/FormMessage";
import kit from "@/app/_components/ui/kit.module.css";
import { initialFormState } from "@/app/_lib/formState";
import { getUploadStatus, uploadDemo, type UploadStatus } from "../_actions";
import { MAX_DEMO_MB } from "../_lib/rules";
import styles from "./UploadForm.module.css";

const steps = ["확장자 검사", "용량 검사", "악성코드 검사 (VirusTotal)", "샌드박스 배포"];

function checkFile(file: File | undefined) {
  if (!file) return null;
  if (!file.name.toLowerCase().endsWith(".zip")) return "zip 파일만 올릴 수 있어요.";
  if (file.size > MAX_DEMO_MB * 1024 * 1024) return `${MAX_DEMO_MB}MB 이하만 올릴 수 있어요.`;
  return null;
}

type Props = {
  used: number;
  capacity: number;
};

export function UploadForm({ used, capacity }: Props) {
  const [state, formAction, pending] = useActionState(uploadDemo, initialFormState);
  const [status, setStatus] = useState<UploadStatus | null>(null);
  const full = used >= capacity;
  const demoId = Number(state.values.demoId);
  const startedAt = Number(state.values.startedAt);

  // 업로드가 끝나면 검사 진행률을 1.5초마다 확인한다 (GET demos/{demoId}/status).
  useEffect(() => {
    if (!state.ok || !demoId) return;
    let stopped = false;
    async function poll() {
      const next = await getUploadStatus(demoId, startedAt);
      if (stopped) return;
      setStatus(next);
      if (!next.done && !next.failed) timer = window.setTimeout(poll, 1500);
    }
    let timer = window.setTimeout(poll, 0);
    return () => {
      stopped = true;
      window.clearTimeout(timer);
    };
  }, [state.ok, demoId, startedAt]);
  const [file, setFile] = useState<{ name: string; size: number; error: string | null } | null>(null);
  const [seenAttempt, setSeenAttempt] = useState(state.attempt);
  // 제출하면 폼이 초기화되므로 선택한 파일 표시도 비운다.
  if (seenAttempt !== state.attempt) {
    setSeenAttempt(state.attempt);
    setFile(null);
  }
  const error = (key: string) => state.errors[key];
  const fileError = file?.error ?? error("file");
  const invalid = (key: string, message: string | null | undefined) =>
    message ? { "aria-invalid": true as const, "aria-describedby": `upload-${key}-error` } : {};

  return (
    <div className={styles.layout}>
      <form action={formAction} className={`${kit.card} ${kit.form}`} key={state.attempt}>
        <div className={kit.field}>
          <label htmlFor="upload-file" className={kit.label}>
            데모 파일 (zip)
          </label>
          <label htmlFor="upload-file" className={`${styles.drop} ${fileError ? styles.dropError : ""}`}>
            <span className={styles.dropIcon} aria-hidden="true">
              📦
            </span>
            {file ? (
              <span>
                <strong>{file.name}</strong>
                <span className={kit.faint}> · {(file.size / 1024 / 1024).toFixed(1)}MB</span>
              </span>
            ) : (
              <span>
                <strong>클릭해서 zip 파일 선택</strong>
                <span className={kit.faint}> · 최대 {MAX_DEMO_MB}MB</span>
              </span>
            )}
          </label>
          <input
            id="upload-file"
            name="file"
            type="file"
            accept=".zip,application/zip"
            className="sr-only"
            {...invalid("file", fileError)}
            onChange={(event) => {
              const selected = event.target.files?.[0];
              setFile(
                selected ? { name: selected.name, size: selected.size, error: checkFile(selected) } : null,
              );
            }}
          />
          <p id="upload-file-error" className={kit.error}>
            {fileError}
          </p>
        </div>

        <div className={kit.field}>
          <label htmlFor="upload-title" className={kit.label}>
            데모 이름
          </label>
          <input
            id="upload-title"
            name="title"
            maxLength={40}
            defaultValue={state.values.title}
            placeholder="예: 실시간 채팅앱"
            className={kit.input}
            {...invalid("title", error("title"))}
          />
          <p id="upload-title-error" className={kit.error}>
            {error("title")}
          </p>
        </div>

        <div className={kit.field}>
          <label htmlFor="upload-description" className={kit.label}>
            한 줄 설명
          </label>
          <textarea
            id="upload-description"
            name="description"
            maxLength={200}
            rows={3}
            defaultValue={state.values.description}
            placeholder="어떤 기술로 무엇을 만들었는지 알려 주세요"
            className={kit.textarea}
            style={{ minHeight: 96 }}
            {...invalid("description", error("description"))}
          />
          <p id="upload-description-error" className={kit.error}>
            {error("description")}
          </p>
        </div>

        <div className={kit.grid2}>
          <fieldset className={kit.fieldset}>
            <legend className={kit.label}>실행 방식</legend>
            <label className={kit.check}>
              <input type="radio" name="runtime" value="web" defaultChecked={state.values.runtime !== "unity"} />
              html / css / js
            </label>
            <label className={kit.check}>
              <input type="radio" name="runtime" value="unity" defaultChecked={state.values.runtime === "unity"} />
              Unity WebGL
            </label>
          </fieldset>
          <div className={kit.field}>
            <label htmlFor="upload-tags" className={kit.label}>
              기술 태그
            </label>
            <input
              id="upload-tags"
              name="tags"
              defaultValue={state.values.tags}
              placeholder="react, websocket"
              className={kit.input}
              aria-describedby="upload-tags-hint"
            />
            <p id="upload-tags-hint" className={kit.hint}>
              쉼표로 구분해요.
            </p>
          </div>
        </div>

        <div className={kit.between}>
          <FormMessage ok={state.ok} message={state.message} />
          <button type="submit" className={kit.btnPrimary} disabled={pending || full || Boolean(file?.error)}>
            {pending ? "올리는 중…" : "업로드"}
          </button>
        </div>
      </form>

      <aside className={kit.stack} aria-label="업로드 안내">
        <section className={kit.card}>
          <h2 className={kit.cardTitle}>데모 인벤토리</h2>
          <p className={full ? kit.error : kit.muted} style={{ marginTop: 8 }}>
            {used}/{capacity}칸 사용 중
            {full && " · 칸이 가득 찼어요. 미니홈피 데모 탭에서 칸을 늘리거나 데모를 지워 주세요."}
          </p>
        </section>
        <section className={kit.card} aria-live="polite">
          <h2 className={kit.cardTitle}>검사 진행 상황</h2>
          <ol className={styles.steps}>
            {steps.map((step, index) => {
              const done = status !== null && (status.done || index < status.step - 1);
              const running = status !== null && !status.done && index === status.step - 1;
              return (
                <li key={step} className={done ? styles.done : undefined}>
                  <span className={styles.stepMark} aria-hidden="true">
                    {done ? "✓" : running ? "…" : index + 1}
                  </span>
                  {step}
                  <span className="sr-only">{done ? " 완료" : running ? " 진행 중" : " 대기"}</span>
                </li>
              );
            })}
          </ol>
        </section>
        <section className={kit.card}>
          <h2 className={kit.cardTitle}>업로드 규격</h2>
          <ul className={styles.rules}>
            <li>zip 최상단에 index.html이 있어야 해요.</li>
            <li>외부 서버 호출은 샌드박스에서 막혀요.</li>
            <li>Unity는 WebGL 빌드 폴더를 그대로 압축해 주세요.</li>
            <li>최대 {MAX_DEMO_MB}MB까지 올릴 수 있어요.</li>
          </ul>
        </section>
      </aside>
    </div>
  );
}
