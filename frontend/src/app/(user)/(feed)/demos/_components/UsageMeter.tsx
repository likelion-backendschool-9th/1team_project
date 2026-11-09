import kit from "@/app/_components/ui/kit.module.css";
import { formatBytes } from "@/app/_lib/format";
import type { DemoUsage } from "@/app/_lib/types/demo";

const MB = 1024 * 1024;

// 1MB 미만 데모가 0.0MB로 보이던 문제 때문에 사용량·남은 용량은 formatBytes로 보여 주고, 한도만 MB로 쓴다.
function toMb(bytes: number) {
  return (bytes / MB).toFixed(1);
}

type Props = {
  usage: DemoUsage;
};

export function UsageMeter({ usage }: Props) {
  return (
    <section className={kit.card} aria-labelledby="demo-usage-title">
      <h2 id="demo-usage-title" className={kit.cardTitle}>
        저장 용량
      </h2>
      <p className={kit.muted} style={{ marginTop: 8 }}>
        {formatBytes(usage.usedBytes)} / {toMb(usage.limitBytes)}MB 사용 · {formatBytes(usage.remainingBytes)} 남음
      </p>
      <progress value={usage.usedBytes} max={usage.limitBytes} style={{ width: "100%" }} aria-label="저장 용량 사용량" />
      <p className={kit.faint}>
        데모 {usage.demoCount}/{usage.maxDemos}개 · 보관 중인 버전과 임시 미리보기도 용량에 포함돼요.
      </p>
    </section>
  );
}
