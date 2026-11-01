"use client";

import { useState } from "react";
import styles from "./FeaturedDemo.module.css";

type Props = {
  title: string;
};

// 데모 북마크 API가 생기면 서버 액션으로 바꾼다 (프론트엔드-청사진 11장).
export function LaterButton({ title }: Props) {
  const [saved, setSaved] = useState(false);

  return (
    <button
      type="button"
      className={`${styles.later} ${saved ? styles.saved : ""}`}
      aria-pressed={saved}
      aria-label={`${title} 나중에 해보기`}
      onClick={() => setSaved((value) => !value)}
    >
      {saved ? "✓ 저장됨" : "나중에 해보기"}
    </button>
  );
}
