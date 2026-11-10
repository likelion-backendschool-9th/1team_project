"use client";

import Link from "next/link";
import { useState, useTransition } from "react";
import { toggleDemoBookmark } from "../demos/[demoId]/_actions";
import styles from "./FeaturedDemo.module.css";

type Props = {
  demoId: number;
  title: string;
  bookmarked: boolean;
};

// 데모 "나중에 해보기" = 데모 북마크 (POST/DELETE /demos/{id}/bookmark).
// 담은 데모는 내 정보 > 북마크 > 데모 탭(/my/bookmarks?type=demo)에서 다시 볼 수 있다.
export function LaterButton({ demoId, title, bookmarked }: Props) {
  const [saved, setSaved] = useState(bookmarked);
  const [message, setMessage] = useState<string | null>(null);
  const [pending, startTransition] = useTransition();

  function toggle() {
    const next = !saved;
    setSaved(next);
    setMessage(null);
    startTransition(async () => {
      const result = await toggleDemoBookmark(demoId, next);
      if (!result.ok) {
        setSaved(!next);
        setMessage(result.message ?? "잠시 후 다시 시도해 주세요.");
      }
    });
  }

  return (
    <>
      <button
        type="button"
        className={`${styles.later} ${saved ? styles.saved : ""}`}
        aria-pressed={saved}
        aria-label={`${title} 나중에 해보기`}
        disabled={pending}
        onClick={toggle}
      >
        {saved ? "✓ 저장됨" : "나중에 해보기"}
      </button>
      {saved && (
        <Link href="/my/bookmarks?type=demo" className={styles.making}>
          저장한 데모 보기
        </Link>
      )}
      <span role="alert" className="sr-only">
        {message}
      </span>
    </>
  );
}
