"use client";

export default function Error({ reset }: { error: Error; reset: () => void }) {
  return (
    <div role="alert">
      <p>피드를 불러오지 못했어요.</p>
      <button type="button" onClick={reset}>
        다시 시도
      </button>
    </div>
  );
}
