"use client";

import { useActionState, useEffect, useRef, useState } from "react";
import { CloseIcon, ImageIcon, LinkIcon } from "@/app/_components/ui/Icons";
import { Avatar } from "@/app/_components/ui/Avatar";
import type { DemoSummary } from "@/app/_lib/types/demo";
import { createFeed, uploadImages, type UploadedImage } from "../_actions";
import {
  initialComposerState,
  MAX_FEED_LENGTH,
  MAX_IMAGE_COUNT,
} from "../_lib/composerState";
import { DemoAttach } from "./DemoAttach";
import styles from "./FeedComposer.module.css";

type FieldProps = {
  defaultValue: string;
  error: string | null;
};

function ComposerField({ defaultValue, error }: FieldProps) {
  const [length, setLength] = useState(defaultValue.length);

  return (
    <>
      <label htmlFor="feed-content" className="sr-only">
        피드 내용
      </label>
      <input
        id="feed-content"
        name="content"
        type="text"
        defaultValue={defaultValue}
        maxLength={MAX_FEED_LENGTH}
        placeholder="무슨 소스를 개발 중인가요? #태그를 달면 태그 피드에 모여요"
        autoComplete="off"
        aria-invalid={error ? true : undefined}
        aria-describedby={error ? "feed-content-error" : undefined}
        className={styles.input}
        onChange={(event) => setLength(event.target.value.length)}
      />
      <span className={styles.count} aria-hidden="true">
        {length} / {MAX_FEED_LENGTH}
      </span>
    </>
  );
}

type Props = {
  nickname: string;
  myDemos: DemoSummary[];
};

// FeedComposer.module.css의 .composer top 값과 맞춘다.
const STICKY_TOP = 12;

export function FeedComposer({ nickname, myDemos }: Props) {
  const [state, formAction, pending] = useActionState(
    createFeed,
    initialComposerState,
  );
  const sectionRef = useRef<HTMLElement>(null);
  const [stuck, setStuck] = useState(false);

  const fileInputRef = useRef<HTMLInputElement>(null);
  const [images, setImages] = useState<UploadedImage[]>([]);
  const [linkVisible, setLinkVisible] = useState(false);
  const [uploadError, setUploadError] = useState<string | null>(null);
  const [uploading, setUploading] = useState(false);

  // 성공 시 이미지·링크 초기화 (attempt 변화 감지용, state.message 의존성 의도적 생략)
  useEffect(() => {
    if (state.message) {
      /* eslint-disable react-hooks/set-state-in-effect */
      setImages([]);
      setLinkVisible(false);
      setUploadError(null);
      /* eslint-enable react-hooks/set-state-in-effect */
    }
  // attempt가 바뀔 때만 실행
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [state.attempt]);

  // 위에 붙어 있는 동안만 그림자를 진하게 해서 떠 있는 상태를 보여 준다.
  useEffect(() => {
    const section = sectionRef.current;
    if (!section) return;
    const observer = new IntersectionObserver(
      ([entry]) =>
        setStuck(
          entry.intersectionRatio < 1 &&
            entry.boundingClientRect.top <= STICKY_TOP + 1,
        ),
      { threshold: 1, rootMargin: `-${STICKY_TOP + 1}px 0px 0px 0px` },
    );
    observer.observe(section);
    return () => observer.disconnect();
  }, []);

  async function handleImageChange(
    e: React.ChangeEvent<HTMLInputElement>,
  ) {
    const files = e.target.files;
    if (!files || files.length === 0) return;

    const remaining = MAX_IMAGE_COUNT - images.length;
    if (remaining <= 0) return;

    setUploading(true);
    setUploadError(null);

    const fd = new FormData();
    Array.from(files)
      .slice(0, remaining)
      .forEach((f) => fd.append("files", f));

    const result = await uploadImages(fd);
    setUploading(false);
    // 같은 파일을 다시 선택할 수 있도록 초기화
    e.target.value = "";

    if ("error" in result) {
      setUploadError(result.error);
      return;
    }
    setImages((prev) => [...prev, ...result.images].slice(0, MAX_IMAGE_COUNT));
  }

  function removeImage(url: string) {
    setImages((prev) => prev.filter((img) => img.url !== url));
  }

  const hasExtras = images.length > 0 || linkVisible;

  return (
    <section
      ref={sectionRef}
      id="feed-composer"
      aria-label="피드 작성"
      className={`${styles.composer} ${stuck ? styles.stuck : ""}`}
    >
      <form action={formAction} className={styles.form}>
        <Avatar name={nickname} size={48} />
        <ComposerField
          key={`field-${state.attempt}`}
          defaultValue={state.values.content}
          error={state.error}
        />
        <DemoAttach
          key={`demo-${state.attempt}`}
          demos={myDemos}
          defaultDemoId={state.values.demoId}
        />

        {/* 숨김 파일 입력 — 이미지 버튼 클릭 시 열린다 */}
        <input
          ref={fileInputRef}
          type="file"
          name="file-picker"
          accept="image/*"
          multiple
          className="sr-only"
          aria-hidden="true"
          tabIndex={-1}
          onChange={handleImageChange}
        />

        <button
          type="button"
          className={styles.tool}
          aria-label="이미지 첨부"
          aria-pressed={images.length > 0}
          disabled={uploading || images.length >= MAX_IMAGE_COUNT}
          onClick={() => fileInputRef.current?.click()}
        >
          <ImageIcon />
        </button>
        <button
          type="button"
          className={`${styles.tool} ${linkVisible ? styles.toolActive : ""}`}
          aria-label="링크 첨부"
          aria-pressed={linkVisible}
          onClick={() => setLinkVisible((v) => !v)}
        >
          <LinkIcon />
        </button>
        <button
          type="submit"
          className={styles.submit}
          disabled={pending || uploading}
        >
          게시
        </button>

        {/* 이미지 URL — 서버 액션으로 전달 (썸네일 아닌 원본 URL) */}
        {images.map((img, i) => (
          <input key={i} type="hidden" name="imageUrls" value={img.url} />
        ))}

        {/* 링크 입력 + 이미지 미리보기 */}
        {hasExtras && (
          <div className={styles.extras}>
            {linkVisible && (
              <div className={styles.linkRow}>
                <LinkIcon aria-hidden="true" />
                <input
                  key={`link-${state.attempt}`}
                  name="linkUrl"
                  type="url"
                  placeholder="https://..."
                  defaultValue={state.values.linkUrl}
                  className={styles.linkInput}
                  aria-label="첨부 링크 URL"
                />
                <button
                  type="button"
                  className={styles.thumbRemove}
                  aria-label="링크 제거"
                  onClick={() => setLinkVisible(false)}
                >
                  <CloseIcon />
                </button>
              </div>
            )}
            {images.length > 0 && (
              <ul className={styles.thumbGrid} aria-label="첨부 이미지">
                {images.map((img) => (
                  <li key={img.url} className={styles.thumb}>
                    {/* thumbnailUrl(400px)로 미리보기, 피드에는 원본 url이 저장됨 */}
                    {/* eslint-disable-next-line @next/next/no-img-element */}
                    <img src={img.thumbnailUrl} alt="" className={styles.thumbImg} />
                    <button
                      type="button"
                      className={styles.thumbRemove}
                      aria-label="이미지 제거"
                      onClick={() => removeImage(img.url)}
                    >
                      <CloseIcon />
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </div>
        )}
      </form>

      {uploadError && (
        <p role="alert" className={styles.error}>
          {uploadError}
        </p>
      )}
      <p id="feed-content-error" role="alert" className={styles.error}>
        {state.error}
      </p>
      <p role="status" className={styles.status}>
        {state.message}
      </p>
    </section>
  );
}
