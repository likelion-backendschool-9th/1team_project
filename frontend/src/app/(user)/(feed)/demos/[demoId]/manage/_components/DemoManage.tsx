"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useActionState, useState, type FormEvent } from "react";
import { ConfirmButton } from "@/app/_components/ui/ConfirmButton";
import { FormMessage } from "@/app/_components/ui/FormMessage";
import kit from "@/app/_components/ui/kit.module.css";
import { initialFormState } from "@/app/_lib/formState";
import type { DemoDetail, DemoUsage, DemoVersion } from "@/app/_lib/types/demo";
import {
  deleteOwnDemo,
  publishDemoVersion,
  refreshDemo,
  unpublishDemo,
  updateDemoInfo,
} from "../../../_actions";
import { UsageMeter } from "../../../_components/UsageMeter";
import { uploadDemoThumbnail, uploadDemoZip } from "../../../_lib/uploadZip";
import { checkDemoZip, MAX_DEMO_MB, MAX_DESCRIPTION, MAX_TITLE } from "../../../upload/_lib/rules";
import { DemoBackendSettings } from "./DemoBackendSettings";
import { DemoVersions } from "./DemoVersions";

type Props = {
  demo: DemoDetail;
  versions: DemoVersion[];
  usage: DemoUsage | null;
};

export function DemoManage({ demo, versions, usage }: Props) {
  const router = useRouter();
  const [state, formAction, saving] = useActionState(updateDemoInfo.bind(null, demo.id), initialFormState);
  const [uploading, setUploading] = useState(false);
  const [notice, setNotice] = useState<{ ok: boolean; message: string } | null>(null);
  const published = demo.status === "ACTIVE";

  async function handleUpload(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = event.currentTarget;
    const file = (new FormData(form).get("file") as File | null) ?? undefined;
    const problem = !file || file.size === 0 ? "zip 파일을 선택해 주세요." : checkDemoZip(file);
    if (problem) {
      setNotice({ ok: false, message: problem });
      return;
    }
    setUploading(true);
    setNotice(null);
    const result = await uploadDemoZip(demo.id, file as File);
    setUploading(false);
    if (!result.ok) {
      setNotice({ ok: false, message: result.message });
      return;
    }
    form.reset();
    setNotice({ ok: true, message: `v${result.data.number}을 저장했어요. 공개하면 방문자에게 보여요.` });
    await refreshDemo(demo.id);
    router.refresh();
  }

  async function handleThumbnail(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = event.currentTarget;
    const file = new FormData(form).get("thumbnail") as File | null;
    if (!file || file.size === 0) {
      setNotice({ ok: false, message: "이미지를 선택해 주세요." });
      return;
    }
    const result = await uploadDemoThumbnail(demo.id, file);
    if (!result.ok) {
      setNotice({ ok: false, message: result.message });
      return;
    }
    form.reset();
    setNotice({ ok: true, message: "썸네일을 바꿨어요." });
    await refreshDemo(demo.id);
    router.refresh();
  }

  return (
    <div className={kit.stack}>
      {usage && <UsageMeter usage={usage} />}
      <section className={`${kit.card} ${kit.stack}`} aria-labelledby="manage-status">
        <div className={kit.between}>
          <h2 id="manage-status" className={kit.cardTitle}>
            공개 상태
          </h2>
          <span className={published ? kit.pillSuccess : kit.pillWarn}>{published ? "공개 중" : "비공개"}</span>
        </div>
        <div className={kit.row}>
          <Link href={`/demos/${demo.id}`} className={`${kit.btnGhost} ${kit.sm}`}>
            상세 보기
          </Link>
          <ConfirmButton
            label="최신 버전 공개"
            title="가장 최근에 올린 버전을 공개할까요?"
            confirmLabel="공개"
            className={`${kit.btnPrimary} ${kit.sm}`}
            onConfirm={() => publishDemoVersion(demo.id)}
            onDone={(result) => {
              setNotice(result);
              router.refresh();
            }}
          />
          {published && (
            <ConfirmButton
              label="공개 내리기"
              title="공개를 내릴까요?"
              description="파일과 버전은 남고, 다른 사람은 더 이상 실행할 수 없어요."
              confirmLabel="내리기"
              className={`${kit.btnGhost} ${kit.sm}`}
              onConfirm={() => unpublishDemo(demo.id)}
              onDone={(result) => {
                setNotice(result);
                router.refresh();
              }}
            />
          )}
          <ConfirmButton
            label="데모 삭제"
            title="이 데모를 삭제할까요?"
            description="댓글과 좋아요도 함께 사라지고, 실행 파일은 바로 접근할 수 없게 돼요."
            confirmLabel="삭제"
            danger
            className={`${kit.btnDanger} ${kit.sm}`}
            onConfirm={() => deleteOwnDemo(demo.id)}
            onDone={(result) => {
              if (result.ok) router.push("/demos");
              else setNotice(result);
            }}
          />
        </div>
        <p role={notice?.ok === false ? "alert" : "status"} className={notice?.ok === false ? kit.error : kit.muted}>
          {notice?.message}
        </p>
      </section>

      <section className={`${kit.card} ${kit.stack}`} aria-labelledby="manage-upload">
        <h2 id="manage-upload" className={kit.cardTitle}>
          새 버전 올리기
        </h2>
        <form onSubmit={handleUpload} className={kit.form}>
          <div className={kit.field}>
            <label htmlFor="manage-file" className={kit.label}>
              zip 파일 (최대 {MAX_DEMO_MB}MB)
            </label>
            <input id="manage-file" name="file" type="file" accept=".zip,application/zip" className={kit.input} />
          </div>
          <div>
            <button type="submit" className={kit.btnPrimary} disabled={uploading}>
              {uploading ? "올리는 중…" : "새 버전 저장"}
            </button>
          </div>
        </form>
      </section>

      <DemoVersions demoId={demo.id} versions={versions} currentVersionId={demo.currentVersionId ?? null} />

      <DemoBackendSettings
        demo={demo}
        mockVersionId={demo.currentVersionId ?? versions.find((version) => version.status === "READY")?.id ?? null}
      />

      <section className={`${kit.card} ${kit.stack}`} aria-labelledby="manage-thumbnail">
        <h2 id="manage-thumbnail" className={kit.cardTitle}>
          썸네일
        </h2>
        <p className={kit.muted}>
          올린 버전을 자동으로 캡처해요. 실패하면 기본 썸네일을 쓰고, 직접 올리면 자동 캡처로 덮어쓰지 않아요.
        </p>
        <form onSubmit={handleThumbnail} className={kit.form}>
          <div className={kit.field}>
            <label htmlFor="manage-thumbnail-file" className={kit.label}>
              이미지 (JPEG·PNG, 2MB 이하)
            </label>
            <input id="manage-thumbnail-file" name="thumbnail" type="file" accept="image/jpeg,image/png" className={kit.input} />
          </div>
          <div>
            <button type="submit" className={`${kit.btnGhost} ${kit.sm}`}>
              썸네일 올리기
            </button>
          </div>
        </form>
      </section>

      <section className={`${kit.card} ${kit.stack}`} aria-labelledby="manage-info">
        <h2 id="manage-info" className={kit.cardTitle}>
          데모 정보
        </h2>
        <form action={formAction} className={kit.form}>
          <div className={kit.field}>
            <label htmlFor="manage-title" className={kit.label}>
              데모 이름
            </label>
            <input
              id="manage-title"
              name="title"
              maxLength={MAX_TITLE}
              defaultValue={state.values.title ?? demo.title}
              className={kit.input}
              aria-invalid={state.errors.title ? true : undefined}
              aria-describedby={state.errors.title ? "manage-title-error" : undefined}
            />
            <p id="manage-title-error" className={kit.error}>
              {state.errors.title}
            </p>
          </div>
          <div className={kit.field}>
            <label htmlFor="manage-description" className={kit.label}>
              한 줄 설명
            </label>
            <textarea
              id="manage-description"
              name="description"
              maxLength={MAX_DESCRIPTION}
              rows={3}
              defaultValue={state.values.description ?? demo.description}
              className={kit.textarea}
              aria-invalid={state.errors.description ? true : undefined}
              aria-describedby={state.errors.description ? "manage-description-error" : undefined}
            />
            <p id="manage-description-error" className={kit.error}>
              {state.errors.description}
            </p>
          </div>
          <div className={kit.field}>
            <label htmlFor="manage-tags" className={kit.label}>
              기술 태그
            </label>
            <input
              id="manage-tags"
              name="tags"
              defaultValue={state.values.tags ?? demo.techTags.join(", ")}
              className={kit.input}
            />
          </div>
          <div className={kit.between}>
            <FormMessage ok={state.ok} message={state.message} />
            <button type="submit" className={kit.btnPrimary} disabled={saving}>
              {saving ? "저장 중…" : "저장"}
            </button>
          </div>
        </form>
      </section>
    </div>
  );
}
