"use client";

import { useRouter } from "next/navigation";
import { useRef, useState, type FormEvent } from "react";
import { FormMessage } from "@/app/_components/ui/FormMessage";
import kit from "@/app/_components/ui/kit.module.css";
import type { DemoUsage } from "@/app/_lib/types/demo";
import { connectDemoBackend, createDemoDraft, deleteOwnDemo, publishDemoVersion } from "../../_actions";
import { BackendFields } from "../../_components/BackendFields";
import { uploadDemoZip } from "../../_lib/uploadZip";
import { UsageMeter } from "../../_components/UsageMeter";
import { checkBackendFields, checkDemoZip, MAX_DEMO_MB, MAX_DESCRIPTION, MAX_TITLE } from "../_lib/rules";
import { ZipPreview } from "./ZipPreview";
import styles from "./UploadForm.module.css";

const steps = ["정보 저장", "zip 검증·업로드", "공개"];

type ApiMode = "none" | "mock" | "external";

const apiModes: { value: ApiMode; label: string; hint: string }[] = [
  { value: "none", label: "백엔드 없이 실행", hint: "html/css/js만으로 동작하는 데모" },
  { value: "mock", label: "Mock API 사용", hint: "업로드 후 코드 편집기에서 가짜 응답을 정하고 공개해요" },
  { value: "external", label: "외부 백엔드 연결", hint: "직접 배포한 서버의 주소를 등록해요" },
];

type Props = {
  used: number;
  capacity: number;
  usage: DemoUsage | null;
};

export function UploadForm({ used, capacity, usage }: Props) {
  const router = useRouter();
  const fileRef = useRef<HTMLInputElement>(null);
  const [file, setFile] = useState<File | null>(null);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [result, setResult] = useState<{ ok: boolean; message: string } | null>(null);
  const [step, setStep] = useState(0);
  const [pending, setPending] = useState(false);
  const [apiMode, setApiMode] = useState<ApiMode>("none");
  const full = used >= capacity;
  const fileError = errors.file ?? (file ? checkDemoZip(file) : null);

  const invalid = (key: string, message: string | null | undefined) =>
    message ? { "aria-invalid": true as const, "aria-describedby": `upload-${key}-error` } : {};

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!file) {
      setErrors({ file: "zip 파일을 선택해 주세요." });
      fileRef.current?.focus();
      return;
    }
    if (checkDemoZip(file)) return;

    const form = new FormData(event.currentTarget);
    const backend = {
      apiBaseUrl: String(form.get("apiBaseUrl") ?? "").trim(),
      healthCheckUrl: String(form.get("healthCheckUrl") ?? "").trim(),
    };
    if (apiMode === "external") {
      const problems = checkBackendFields(backend.apiBaseUrl, backend.healthCheckUrl);
      const first = Object.keys(problems)[0];
      if (first) {
        setErrors(problems);
        document.getElementById(`upload-backend-${first}`)?.focus();
        return;
      }
    }
    setPending(true);
    setErrors({});
    setResult(null);
    try {
      setStep(1);
      const draft = await createDemoDraft({
        title: String(form.get("title") ?? ""),
        description: String(form.get("description") ?? ""),
        tags: String(form.get("tags") ?? ""),
      });
      if (!draft.ok || draft.demoId === undefined) {
        setErrors(draft.errors ?? {});
        setResult(draft);
        return;
      }

      if (apiMode === "external") {
        const connected = await connectDemoBackend(draft.demoId, backend);
        if (!connected.ok) {
          await deleteOwnDemo(draft.demoId);
          setErrors({ apiBaseUrl: connected.message });
          setResult({ ok: false, message: "외부 백엔드를 연결하지 못했어요." });
          return;
        }
      }

      setStep(2);
      const upload = await uploadDemoZip(draft.demoId, file);
      if (!upload.ok) {
        // 파일 없는 초안을 남기면 다시 시도할 때 인벤토리만 찬다
        await deleteOwnDemo(draft.demoId);
        setErrors({ file: upload.message });
        setResult({ ok: false, message: "zip을 올리지 못했어요." });
        return;
      }

      // Mock은 버전마다 저장되므로 응답을 정한 새 버전을 코드 편집기에서 공개한다
      if (apiMode === "mock") {
        router.push(`/demos/studio?demoId=${draft.demoId}&versionId=${upload.data.id}`);
        return;
      }

      if (form.get("publish") === "on") {
        setStep(3);
        const published = await publishDemoVersion(draft.demoId, upload.data.id);
        if (!published.ok) {
          setResult({ ok: false, message: `파일은 저장했지만 공개하지 못했어요: ${published.message}` });
          router.push(`/demos/${draft.demoId}/manage`);
          return;
        }
        router.push(`/demos/${draft.demoId}`);
        return;
      }
      router.push(`/demos/${draft.demoId}/manage`);
    } finally {
      setPending(false);
      setStep(0);
    }
  }

  return (
    <div className={styles.layout}>
      <form onSubmit={handleSubmit} className={`${kit.card} ${kit.form}`} noValidate>
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
            ref={fileRef}
            id="upload-file"
            type="file"
            accept=".zip,application/zip"
            className="sr-only"
            {...invalid("file", fileError)}
            onChange={(event) => {
              setFile(event.target.files?.[0] ?? null);
              setErrors((current) => ({ ...current, file: "" }));
            }}
          />
          <p id="upload-file-error" className={kit.error}>
            {fileError}
          </p>
          {file && !fileError && <ZipPreview key={`${file.name}-${file.size}-${file.lastModified}`} file={file} />}
        </div>

        <div className={kit.field}>
          <label htmlFor="upload-title" className={kit.label}>
            데모 이름
          </label>
          <input
            id="upload-title"
            name="title"
            maxLength={MAX_TITLE}
            placeholder="예: 실시간 채팅앱"
            className={kit.input}
            {...invalid("title", errors.title)}
          />
          <p id="upload-title-error" className={kit.error}>
            {errors.title}
          </p>
        </div>

        <div className={kit.field}>
          <label htmlFor="upload-description" className={kit.label}>
            한 줄 설명
          </label>
          <textarea
            id="upload-description"
            name="description"
            maxLength={MAX_DESCRIPTION}
            rows={3}
            placeholder="어떤 기술로 무엇을 만들었는지 알려 주세요"
            className={kit.textarea}
            style={{ minHeight: 96 }}
            {...invalid("description", errors.description)}
          />
          <p id="upload-description-error" className={kit.error}>
            {errors.description}
          </p>
        </div>

        <div className={kit.field}>
          <label htmlFor="upload-tags" className={kit.label}>
            기술 태그
          </label>
          <input
            id="upload-tags"
            name="tags"
            placeholder="react, websocket"
            className={kit.input}
            aria-describedby="upload-tags-hint"
          />
          <p id="upload-tags-hint" className={kit.hint}>
            쉼표로 구분해요.
          </p>
        </div>

        <fieldset className={kit.fieldset}>
          <legend className={kit.label}>API 연결</legend>
          {apiModes.map((mode) => (
            <label key={mode.value} className={kit.check}>
              <input
                type="radio"
                name="apiMode"
                value={mode.value}
                checked={apiMode === mode.value}
                onChange={() => setApiMode(mode.value)}
              />
              <span>
                {mode.label} <span className={kit.faint}>· {mode.hint}</span>
              </span>
            </label>
          ))}
        </fieldset>

        {apiMode === "external" && <BackendFields idPrefix="upload-backend" errors={errors} />}

        <label className={kit.check}>
          <input type="checkbox" name="publish" defaultChecked />
          업로드가 끝나면 바로 공개
        </label>

        <div className={kit.between}>
          <FormMessage ok={result?.ok ?? null} message={result?.message ?? null} />
          <button type="submit" className={kit.btnPrimary} disabled={pending || full || Boolean(fileError)}>
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
        {usage && <UsageMeter usage={usage} />}
        <section className={kit.card} aria-live="polite">
          <h2 className={kit.cardTitle}>진행 상황</h2>
          <ol className={styles.steps}>
            {steps.map((name, index) => {
              const done = step > index + 1;
              const running = step === index + 1;
              return (
                <li key={name} className={done ? styles.done : undefined}>
                  <span className={styles.stepMark} aria-hidden="true">
                    {done ? "✓" : running ? "…" : index + 1}
                  </span>
                  {name}
                  <span className="sr-only">{done ? " 완료" : running ? " 진행 중" : " 대기"}</span>
                </li>
              );
            })}
          </ol>
        </section>
        <section className={kit.card}>
          <h2 className={kit.cardTitle}>업로드 규격</h2>
          <ul className={styles.rules}>
            <li>zip 최상위에 index.html이 있어야 해요.</li>
            <li>zip {MAX_DEMO_MB}MB, 압축 해제 후 30MB, 파일 200개까지 올릴 수 있어요.</li>
            <li>Vite는 base: &apos;./&apos;, CRA는 homepage: &apos;.&apos;로 빌드해 주세요.</li>
            <li>라우터는 Hash Router를 권장해요.</li>
            <li>외부 서버는 등록한 백엔드 주소로만 호출할 수 있어요. 쿠키·브라우저 저장소는 쓸 수 없어요.</li>
          </ul>
        </section>
      </aside>
    </div>
  );
}
