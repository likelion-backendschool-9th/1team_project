import type { Metadata } from "next";
import { redirect } from "next/navigation";
import { Suspense } from "react";
import { ConfirmButton } from "@/app/_components/ui/ConfirmButton";
import { ChipIcon, ChipPack } from "@/app/_components/ui/ChipIcon";
import kit from "@/app/_components/ui/kit.module.css";
import { formatCount, formatWon } from "@/app/_lib/format";
import { CHIP_PRICE_WON, chipsToWon } from "@/app/_lib/policy";
import { getMe } from "@/app/(user)/_lib/api";
import { purchaseChips } from "../_actions";
import { getChipHistory, getPackages } from "../_lib/api";
import styles from "../_components/My.module.css";
import { ChipHistoryList } from "./_components/ChipHistoryList";

export const metadata: Metadata = { title: "칩 충전", robots: { index: false } };

async function CoinsContent() {
  const me = await getMe();
  if (!me) redirect("/login?redirect=/my/coins");

  // 첫 페이지만 서버에서 가져옴. 이후 페이지는 ChipHistoryList 클라이언트에서 이어 받음
  const [packages, initialHistory] = await Promise.all([getPackages(), getChipHistory(1)]);

  return (
    <>
      <section className={styles.balance} aria-label="보유 칩">
        <div>
          <p className={kit.strong}>
            <ChipIcon size={22} /> 보유 칩
          </p>
          <p className={styles.balanceValue}>{formatCount(me.chipBalance)}개</p>
        </div>
        <p style={{ fontSize: 14 }}>
          칩 1개 = {formatWon(CHIP_PRICE_WON)} · 유료 글 잠금해제, 미니홈피 후원, 데모 인벤토리 확장에 써요.
          <br />
          블로그 구독과 딥소스 플러스는 칩이 아니라 월 자동이체로 결제돼요.
        </p>
      </section>

      <section aria-labelledby="packages-title">
        <h2 id="packages-title" className={kit.cardTitle} style={{ marginBottom: 12 }}>
          충전하기
        </h2>
        <ul className={kit.grid4}>
          {packages.map((item) => (
            <li key={item.id} className={styles.package}>
              <ChipPack chips={item.chips} />
              <span className={styles.packageChips}>{formatCount(item.chips)}칩</span>
              <span className={kit.muted}>{formatWon(chipsToWon(item.chips))}</span>
              <ConfirmButton
                label="충전"
                title={`${formatCount(item.chips)}칩을 충전할까요?`}
                description={`${formatWon(chipsToWon(item.chips))}이 결제돼요.`}
                confirmLabel="결제하기"
                className={`${kit.btnPrimary} ${kit.sm}`}
                onConfirm={purchaseChips.bind(null, item.id)}
              />
            </li>
          ))}
        </ul>
      </section>

      <section className={kit.card} aria-labelledby="history-title">
        <h2 id="history-title" className={kit.cardTitle}>
          충전·사용 내역
        </h2>
        <ChipHistoryList initial={initialHistory} />
      </section>
    </>
  );
}

export default function CoinsPage() {
  return (
    <>
      <h1 className={kit.pageTitle}>칩</h1>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <CoinsContent />
      </Suspense>
    </>
  );
}
