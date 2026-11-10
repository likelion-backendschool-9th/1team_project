"use client";

import { useEffect, useState } from "react";
import { formatRelativeTime, nextRelativeTimeDelay } from "@/app/_lib/format";

type Props = {
  iso: string;
  /** 서버가 그린 시각. 첫 화면은 이 값으로 맞추고, 이후에는 브라우저 시각으로 갱신한다. */
  now: number;
};

export function RelativeTime({ iso, now }: Props) {
  const [current, setCurrent] = useState(now);

  useEffect(() => {
    let timer = 0;
    const schedule = () => {
      const at = Date.now();
      setCurrent(at);
      const delay = nextRelativeTimeDelay(iso, at);
      if (delay === 0) return;
      timer = window.setTimeout(schedule, delay);
    };
    schedule();
    return () => window.clearTimeout(timer);
  }, [iso]);

  return <time dateTime={iso}>{formatRelativeTime(iso, current)}</time>;
}
