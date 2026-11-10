"use client";

import { useRouter } from "next/navigation";
import { useMemo, useState } from "react";
import kit from "@/app/_components/ui/kit.module.css";
import { createDemoDraft, deleteOwnDemo, publishDemoVersion, refreshDemo } from "../../_actions";
import { saveDemoConfig, saveEditorVersion } from "../../_lib/uploadZip";
import { MAX_DESCRIPTION, MAX_TITLE } from "../../upload/_lib/rules";
import {
  checkJson,
  composeStudioDocument,
  MAX_MOCK_CONFIG_BYTES,
  MAX_SAMPLE_BYTES,
  resolveMockRows,
  type StudioCode,
} from "../_lib/studioDocument";
import { CodeEditor, type EditorLanguage } from "./CodeEditor";
import styles from "./DemoStudio.module.css";

type TabKey = keyof StudioCode;

const allTabs: { key: TabKey; label: string; language: EditorLanguage }[] = [
  { key: "html", label: "HTML", language: "html" },
  { key: "css", label: "CSS", language: "css" },
  { key: "js", label: "JavaScript", language: "javascript" },
  { key: "mockConfig", label: "Mock API", language: "json" },
  { key: "sampleData", label: "샘플 데이터", language: "json" },
];

type Props = {
  initial: StudioCode;
  /** 있으면 이 데모의 새 버전으로 저장한다. 없으면 새 데모를 만든다. */
  target: { demoId: number; versionId: number } | null;
  /** false면 ZIP으로 올린 버전이라 Mock·샘플 데이터만 고친다. */
  editable: boolean;
};

export function DemoStudio({ initial, target, editable }: Props) {
  const router = useRouter();
  const tabs = useMemo(() => (editable ? allTabs : allTabs.slice(3)), [editable]);
  const [code, setCode] = useState(initial);
  const [resetCount, setResetCount] = useState(0);
  const [tab, setTab] = useState<TabKey>(tabs[0].key);
  const [run, setRun] = useState<{ id: number; srcdoc: string } | null>(null);
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [publish, setPublish] = useState(true);
  const [saving, setSaving] = useState(false);
  const [notice, setNotice] = useState<{ ok: boolean; message: string } | null>(null);

  const mockCheck = checkJson(code.mockConfig, MAX_MOCK_CONFIG_BYTES);
  const sampleCheck = checkJson(code.sampleData, MAX_SAMPLE_BYTES);
  const jsonError = !mockCheck.ok ? mockCheck.message : !sampleCheck.ok ? sampleCheck.message : null;
  const mockRows = resolveMockRows(code.mockConfig, code.sampleData);
  const activeTab = tabs.find((item) => item.key === tab) ?? tabs[0];

  function handleRun() {
    setRun((current) => ({ id: (current?.id ?? 0) + 1, srcdoc: composeStudioDocument(code) }));
  }

  function handleReset() {
    setCode(initial);
    setResetCount((count) => count + 1);
    setRun(null);
    setNotice(null);
  }

  async function saveTo(demoId: number) {
    if (editable) return saveEditorVersion(demoId, code);
    return saveDemoConfig(demoId, target!.versionId, { mockConfig: code.mockConfig, sampleData: code.sampleData });
  }

  async function handleSave() {
    if (jsonError) {
      setNotice({ ok: false, message: jsonError });
      return;
    }
    setSaving(true);
    setNotice(null);
    try {
      let demoId = target?.demoId;
      let created = false;
      if (demoId === undefined) {
        const draft = await createDemoDraft({ title, description, tags: "" });
        if (!draft.ok || draft.demoId === undefined) {
          setNotice({ ok: false, message: draft.errors?.title ?? draft.message });
          return;
        }
        demoId = draft.demoId;
        created = true;
      }

      const saved = await saveTo(demoId);
      if (!saved.ok) {
        if (created) await deleteOwnDemo(demoId);
        setNotice({ ok: false, message: saved.message });
        return;
      }
      if (publish) {
        const published = await publishDemoVersion(demoId, saved.data.id);
        if (!published.ok) {
          setNotice({ ok: false, message: `저장했지만 공개하지 못했어요: ${published.message}` });
          router.push(`/demos/${demoId}/manage`);
          return;
        }
      }
      await refreshDemo(demoId);
      router.push(`/demos/${demoId}/manage`);
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className={kit.stack}>
      <div className={styles.layout}>
        <section className={`${kit.card} ${kit.stack}`} aria-label="코드 편집">
          <div className={styles.tabs} role="group" aria-label="편집할 파일">
            {tabs.map((item) => (
              <button
                key={item.key}
                type="button"
                className={`${styles.tab} ${item.key === activeTab.key ? styles.tabActive : ""}`}
                aria-pressed={item.key === activeTab.key}
                onClick={() => setTab(item.key)}
              >
                {item.label}
              </button>
            ))}
          </div>
          <CodeEditor
            key={`${activeTab.key}-${resetCount}`}
            label={`${activeTab.label} 편집기`}
            language={activeTab.language}
            value={code[activeTab.key]}
            onChange={(value) => setCode((current) => ({ ...current, [activeTab.key]: value }))}
          />
          <p role={jsonError ? "alert" : "status"} className={jsonError ? kit.error : kit.faint}>
            {jsonError ?? "fetch(\"__mock/products\")처럼 상대 경로로 Mock API를 부를 수 있어요."}
          </p>
          <div className={kit.row}>
            {editable && (
              <button type="button" className={kit.btnPrimary} onClick={handleRun}>
                ▶ 실행
              </button>
            )}
            <button type="button" className={kit.btnGhost} onClick={handleReset}>
              초기화
            </button>
          </div>
        </section>

        <section className={`${kit.card} ${kit.stack}`} aria-label="미리보기">
          <h2 className={kit.cardTitle}>미리보기</h2>
          {!editable ? (
            <p className={kit.muted}>
              zip으로 올린 버전은 파일을 여기서 고칠 수 없어요. Mock API와 샘플 데이터만 바꿔 새 버전으로 저장해요.
            </p>
          ) : run ? (
            // 사용자 코드는 opaque origin의 sandbox에서만 돈다
            <iframe key={run.id} title="코드 실행 미리보기" srcDoc={run.srcdoc} sandbox="allow-scripts" className={styles.preview} />
          ) : (
            <p className={kit.muted}>▶ 실행을 누르면 여기에 나타나요. 외부 통신은 막혀 있어요.</p>
          )}
          <h3 className={kit.strong}>Mock API {mockRows.length}개</h3>
          {mockRows.length === 0 ? (
            <p className={kit.faint}>등록된 엔드포인트가 없어요.</p>
          ) : (
            <pre className={styles.jsonView}>
              {mockRows
                .map((row) => `${row.method} /__mock${row.path} → ${row.status}\n${JSON.stringify(row.response ?? null, null, 2)}`)
                .join("\n\n")}
            </pre>
          )}
          <p className={kit.faint}>
            실제 개인정보를 넣지 마세요. 공개한 데모의 Mock 응답과 샘플 데이터는 누구나 볼 수 있어요. 쓰기 요청도 등록한 응답만
            돌려주고 아무것도 저장하지 않아요.
          </p>
        </section>
      </div>

      <section className={`${kit.card} ${kit.form}`} aria-label="저장">
        {!target && (
          <>
            <div className={kit.field}>
              <label htmlFor="studio-title" className={kit.label}>
                데모 이름
              </label>
              <input
                id="studio-title"
                maxLength={MAX_TITLE}
                value={title}
                onChange={(event) => setTitle(event.target.value)}
                className={kit.input}
              />
            </div>
            <div className={kit.field}>
              <label htmlFor="studio-description" className={kit.label}>
                한 줄 설명
              </label>
              <input
                id="studio-description"
                maxLength={MAX_DESCRIPTION}
                value={description}
                onChange={(event) => setDescription(event.target.value)}
                className={kit.input}
              />
            </div>
          </>
        )}
        <label className={kit.check}>
          <input type="checkbox" checked={publish} onChange={(event) => setPublish(event.target.checked)} />
          저장하고 바로 공개
        </label>
        <div className={kit.between}>
          <p role={notice?.ok === false ? "alert" : "status"} className={notice?.ok === false ? kit.error : kit.muted}>
            {notice?.message}
          </p>
          <button type="button" className={kit.btnPrimary} disabled={saving || Boolean(jsonError)} onClick={handleSave}>
            {saving ? "저장 중…" : target ? "새 버전으로 저장" : "데모로 저장"}
          </button>
        </div>
      </section>
    </div>
  );
}
