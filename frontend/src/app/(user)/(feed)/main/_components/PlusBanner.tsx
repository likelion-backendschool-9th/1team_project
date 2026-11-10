import Link from "next/link";
import { formatWon } from "@/app/_lib/format";
import { PLUS_BENEFITS, PLUS_PRICE_WON } from "@/app/_lib/policy";
import styles from "./PlusBanner.module.css";

// 피드 가운데에 두는 딥소스 플러스 구독 안내. 이미 플러스인 사람에게는 그리지 않는다.
export function PlusBanner() {
  return (
    <aside className={styles.banner} aria-labelledby="plus-banner-title">
      <div className={styles.text}>
        <p className={styles.label}>DIPSOURCE PLUS</p>
        <h2 id="plus-banner-title" className={styles.title}>
          내 미니홈피를 더 나답게, 딥소스 플러스
        </h2>
        <ul className={styles.benefits}>
          {PLUS_BENEFITS.slice(0, 3).map((benefit) => (
            <li key={benefit}>{benefit}</li>
          ))}
        </ul>
      </div>
      <div className={styles.side}>
        <p className={styles.price}>
          월 <strong>{formatWon(PLUS_PRICE_WON)}</strong>
        </p>
        <Link href="/my/subscription" className={styles.cta}>
          플러스 알아보기 →
        </Link>
      </div>
    </aside>
  );
}
