"use client";

import kit from "@/app/_components/ui/kit.module.css";

export default function Error({ retry }: { error: unknown; retry: () => void }) {
  return (
    <div role="alert" className={kit.card}>
      <p>화면을 불러오지 못했어요. 잠시 후 다시 시도해 주세요.</p>
      <button type="button" className={kit.btnDark} onClick={() => retry()}>
        다시 시도
      </button>
    </div>
  );
}
