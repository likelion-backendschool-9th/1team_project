"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState, useTransition } from "react";
import { ConfirmButton } from "@/app/_components/ui/ConfirmButton";
import kit from "@/app/_components/ui/kit.module.css";
import { formatDate } from "@/app/_lib/format";
import type { DemoVersion } from "@/app/_lib/types/demo";
import { createVersionPreview, deleteDemoVersion, restoreDemoVersion } from "../../../_actions";

const thumbnailLabels: Record<DemoVersion["thumbnailStatus"], string> = {
  PENDING: "썸네일 생성 대기",
  READY: "썸네일 준비됨",
  FAILED: "썸네일 실패 · 기본 이미지 사용",
  SKIPPED: "썸네일 없음",
};

type Props = {
  demoId: number;
  versions: DemoVersion[];
  currentVersionId: number | null;
};

export function DemoVersions({ demoId, versions, currentVersionId }: Props) {
  const router = useRouter();
  const [notice, setNotice] = useState<{ ok: boolean; message: string } | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [pending, startTransition] = useTransition();

  function handleDone(result: { ok: boolean; message: string }) {
    setNotice(result);
    router.refresh();
  }

  function openPreview(versionId: number) {
    startTransition(async () => {
      const result = await createVersionPreview(demoId, versionId);
      if (result.ok && result.url) setPreviewUrl(result.url);
      else setNotice(result);
    });
  }

  return (
    <section className={`${kit.card} ${kit.stack}`} aria-labelledby="manage-versions">
      <h2 id="manage-versions" className={kit.cardTitle}>
        버전
      </h2>
      {versions.length === 0 ? (
        <p className={kit.empty}>아직 올린 버전이 없어요.</p>
      ) : (
        <ul className={kit.list}>
          {versions.map((version) => (
            <li key={version.id} className={kit.listItem}>
              <div className={kit.grow}>
                <p className={kit.strong}>
                  v{version.number}{" "}
                  {version.current && <span className={kit.pillSuccess}>공개 중</span>}
                </p>
                <p className={kit.faint}>
                  {formatDate(version.createdAt)} · 파일 {version.fileCount}개 ·{" "}
                  {(version.totalBytes / 1024 / 1024).toFixed(2)}MB · {thumbnailLabels[version.thumbnailStatus]}
                </p>
                {version.thumbnailStatus === "FAILED" && version.thumbnailError && (
                  <p className={kit.faint}>원인: {version.thumbnailError}</p>
                )}
              </div>
              <div className={kit.row}>
                <button
                  type="button"
                  className={`${kit.btnGhost} ${kit.sm}`}
                  disabled={pending}
                  onClick={() => openPreview(version.id)}
                >
                  미리보기
                </button>
                <Link
                  href={`/demos/studio?demoId=${demoId}&versionId=${version.id}`}
                  className={`${kit.btnGhost} ${kit.sm}`}
                >
                  코드·Mock 편집
                </Link>
                {!version.current && (
                  <>
                    <ConfirmButton
                      label={currentVersionId === null ? "이 버전 공개" : "이 버전으로 복구"}
                      title={`v${version.number}을 공개할까요?`}
                      description="다시 빌드하지 않고 보관된 파일을 그대로 공개해요. 파일이 온전하지 않으면 지금 공개 중인 버전을 유지해요."
                      confirmLabel="공개"
                      className={`${kit.btnPrimary} ${kit.sm}`}
                      onConfirm={() => restoreDemoVersion(demoId, version.id, currentVersionId)}
                      onDone={handleDone}
                    />
                    <ConfirmButton
                      label="삭제"
                      title={`v${version.number}을 지울까요?`}
                      description="파일이 지워지고 저장 용량이 돌아와요. 되돌릴 수 없어요."
                      confirmLabel="삭제"
                      danger
                      className={`${kit.btnDanger} ${kit.sm}`}
                      onConfirm={() => deleteDemoVersion(demoId, version.id)}
                      onDone={handleDone}
                    />
                  </>
                )}
              </div>
            </li>
          ))}
        </ul>
      )}
      <p role={notice?.ok === false ? "alert" : "status"} className={notice?.ok === false ? kit.error : kit.muted}>
        {notice?.message}
      </p>
      {previewUrl && (
        <iframe
          title="버전 미리보기"
          src={previewUrl}
          sandbox="allow-scripts"
          referrerPolicy="no-referrer"
          style={{ width: "100%", aspectRatio: "16 / 10", border: 0, borderRadius: 12 }}
        />
      )}
    </section>
  );
}
