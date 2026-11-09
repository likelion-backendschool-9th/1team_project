"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useActionState, useState } from "react";
import { ConfirmButton } from "@/app/_components/ui/ConfirmButton";
import { FormMessage } from "@/app/_components/ui/FormMessage";
import kit from "@/app/_components/ui/kit.module.css";
import { initialFormState } from "@/app/_lib/formState";
import type { DemoDetail } from "@/app/_lib/types/demo";
import { disconnectDemoBackend, saveDemoBackend } from "../../../_actions";
import { BackendFields } from "../../../_components/BackendFields";

type Props = {
  demo: DemoDetail;
  /** Mock을 설정할 버전. 올린 버전이 없으면 없다. */
  mockVersionId: number | null;
};

export function DemoBackendSettings({ demo, mockVersionId }: Props) {
  const router = useRouter();
  const [state, formAction, saving] = useActionState(saveDemoBackend.bind(null, demo.id), initialFormState);
  const [notice, setNotice] = useState<{ ok: boolean; message: string } | null>(null);
  const connected = Boolean(demo.backend?.apiBaseUrl);

  return (
    <section className={`${kit.card} ${kit.stack}`} aria-labelledby="manage-backend">
      <div className={kit.between}>
        <h2 id="manage-backend" className={kit.cardTitle}>
          외부 백엔드
        </h2>
        <span className={connected ? kit.pillAccent : kit.pill}>{connected ? "연결됨" : "연결 안 함"}</span>
      </div>
      <p className={kit.muted}>
        정적 파일만으로 실행하려면 비워 두세요. 가짜 응답이 필요하면{" "}
        {mockVersionId === null ? (
          "버전을 올린 뒤 Mock API를 설정할 수 있어요."
        ) : (
          <Link href={`/demos/studio?demoId=${demo.id}&versionId=${mockVersionId}`}>Mock API를 설정</Link>
        )}
        {mockVersionId !== null && "하세요. Mock과 외부 백엔드는 함께 쓸 수 있어요."}
      </p>
      <form action={formAction} className={kit.form} noValidate>
        <BackendFields
          key={state.attempt}
          idPrefix="manage-backend"
          errors={state.errors}
          apiBaseUrl={state.values.apiBaseUrl ?? demo.backend?.apiBaseUrl}
          healthCheckUrl={state.values.healthCheckUrl ?? demo.backend?.healthCheckUrl}
        />
        <p className={kit.faint}>
          호출 확인은 버전 목록의 미리보기나 공개 후 실행 화면에서 해 주세요. 업로드 화면과 코드 편집기의 미리보기는 외부
          통신을 막아요.
        </p>
        <div className={kit.between}>
          <FormMessage ok={state.ok} message={state.message} />
          <div className={kit.row}>
            {connected && (
              <ConfirmButton
                label="연결 해제"
                title="외부 백엔드 연결을 해제할까요?"
                description="데모 파일은 그대로고, 이 데모에서 외부 서버로 통신할 수 없게 돼요."
                confirmLabel="해제"
                className={`${kit.btnGhost} ${kit.sm}`}
                onConfirm={() => disconnectDemoBackend(demo.id)}
                onDone={(result) => {
                  setNotice(result);
                  router.refresh();
                }}
              />
            )}
            <button type="submit" className={kit.btnPrimary} disabled={saving}>
              {saving ? "저장 중…" : "연결 저장"}
            </button>
          </div>
        </div>
        <p role={notice?.ok === false ? "alert" : "status"} className={notice?.ok === false ? kit.error : kit.muted}>
          {notice?.message}
        </p>
      </form>
    </section>
  );
}
