"use client";

import { useRouter } from "next/navigation";
import { useState, useTransition } from "react";
import kit from "@/app/_components/ui/kit.module.css";
import type { DemoBackendStatus } from "@/app/_lib/types/demo";
import { checkDemoBackendHealth } from "../_actions";
import styles from "./DemoDetail.module.css";

type Props = {
  demoId: number;
  host: string;
  status: DemoBackendStatus;
  /** 서버에서 미리 포맷한 마지막 확인 시각 */
  lastChecked: string | null;
  canCheck: boolean;
};

const statusView: Record<DemoBackendStatus, { label: string; className: string }> = {
  ONLINE: { label: "온라인", className: kit.pillSuccess },
  OFFLINE: { label: "오프라인", className: kit.pillDanger },
  UNKNOWN: { label: "상태 확인 전", className: kit.pill },
  NOT_CONFIGURED: { label: "연결 안 함", className: kit.pill },
};

export function DemoBackendPanel({ demoId, host, status, lastChecked, canCheck }: Props) {
  const router = useRouter();
  const [message, setMessage] = useState<string | null>(null);
  const [pending, startTransition] = useTransition();
  const view = statusView[status];

  function handleCheck() {
    setMessage(null);
    startTransition(async () => {
      const result = await checkDemoBackendHealth(demoId);
      if (!result.ok) setMessage(result.message);
      router.refresh();
    });
  }

  return (
    <div className={styles.backend}>
      <div className={kit.between}>
        <p>
          <strong>외부 백엔드</strong> <span className={styles.backendHost}>{host}</span>
        </p>
        <span className={view.className}>{view.label}</span>
      </div>
      <p className={kit.faint}>
        실행하면 브라우저가 이 서버와 직접 통신해요. 찍먹이 운영하는 서버가 아니니 비밀번호 같은 민감한 정보는 입력하지
        마세요.
      </p>
      {(lastChecked || canCheck) && (
        <div className={kit.between}>
          <p className={kit.faint}>{lastChecked ? `마지막 확인 ${lastChecked}` : "아직 확인하지 않았어요"}</p>
          {canCheck && (
            <button type="button" className={`${kit.btnGhost} ${kit.sm}`} disabled={pending} onClick={handleCheck}>
              {pending ? "확인 중…" : "상태 확인"}
            </button>
          )}
        </div>
      )}
      <p role="alert" className={kit.error}>
        {message}
      </p>
    </div>
  );
}
