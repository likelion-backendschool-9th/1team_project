"use client";

import { useRef, useState } from "react";
import kit from "@/app/_components/ui/kit.module.css";
import styles from "./EventForm.module.css";

type Props = {
  imageUrl: string;
  onChange: (imageUrl: string) => void;
  onUploadingChange: (uploading: boolean) => void;
  /** 서버 액션 검증 오류 */
  serverError?: string;
};

const MAX_BYTES = 10 * 1024 * 1024; // 백엔드 ImageService와 같은 10MB
const ACCEPT = "image/jpeg,image/png,image/gif,image/webp";

// 파일은 Server Action(기본 1MB 제한)을 거치지 않고 브라우저에서 바로 /api/v1로 보낸다.
// next.config의 리라이트가 백엔드로 넘겨 주고, 로그인 쿠키도 같이 간다.
async function upload(file: File): Promise<{ url: string } | { error: string }> {
  const body = new FormData();
  body.append("file", file); // 백엔드 @RequestPart("file")와 이름이 같아야 한다
  try {
    const res = await fetch("/api/v1/admin/events/images", { method: "POST", body });
    const data = await res.json().catch(() => null);
    if (res.ok && typeof data?.data?.url === "string") return { url: data.data.url };
    if (res.status === 401) return { error: "로그인이 만료됐어요. 새로고침한 뒤 다시 시도해 주세요." };
    return { error: data?.error?.message ?? "이미지를 올리지 못했어요." };
  } catch {
    return { error: "이미지를 올리는 중 오류가 났어요." };
  }
}

/** 이벤트 대표 이미지. 고르면 바로 올리고, 받은 주소를 hidden input(imageUrl)으로 폼에 싣는다. */
export function EventImageField({ imageUrl, onChange, onUploadingChange, serverError }: Props) {
  const inputRef = useRef<HTMLInputElement>(null);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleFile(event: React.ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    event.target.value = ""; // 같은 파일을 다시 골라도 onChange가 불리게 비운다
    if (!file) return;
    if (file.size > MAX_BYTES) {
      setError("10MB 이하 이미지만 올릴 수 있어요.");
      return;
    }
    setError(null);
    setUploading(true);
    onUploadingChange(true);
    const result = await upload(file);
    setUploading(false);
    onUploadingChange(false);
    if ("error" in result) setError(result.error);
    else onChange(result.url);
  }

  return (
    <div className={kit.field}>
      <span className={kit.label} id="event-image-label">
        대표 이미지
      </span>
      <input type="hidden" name="imageUrl" value={imageUrl} />
      <div className={styles.imageRow}>
        <div className={styles.imagePreview}>
          {imageUrl ? <img src={imageUrl} alt="선택한 대표 이미지" /> : <span>이미지 없음</span>}
        </div>
        <div className={kit.stack}>
          <input
            ref={inputRef}
            type="file"
            accept={ACCEPT}
            className="sr-only"
            aria-labelledby="event-image-label"
            onChange={handleFile}
          />
          <div className={kit.row}>
            <button
              type="button"
              className={`${kit.btnGhost} ${kit.sm}`}
              disabled={uploading}
              onClick={() => inputRef.current?.click()}
            >
              {uploading ? "올리는 중…" : imageUrl ? "이미지 바꾸기" : "이미지 올리기"}
            </button>
            {imageUrl && !uploading && (
              <button type="button" className={`${kit.btnGhost} ${kit.sm}`} onClick={() => onChange("")}>
                이미지 빼기
              </button>
            )}
          </div>
          <p className={kit.faint}>JPG·PNG·GIF·WebP, 10MB 이하. 목록에서는 가운데 부분이 잘려 보여요.</p>
          <p className={kit.error} role="alert">
            {error ?? serverError}
          </p>
        </div>
      </div>
    </div>
  );
}
