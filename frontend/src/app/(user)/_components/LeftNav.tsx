import Link from "next/link";
import { Suspense } from "react";
import { NavLinks } from "./NavLinks";
import { RankingWidget } from "./RankingWidget";
import styles from "./LeftNav.module.css";

export function LeftNav() {
  return (
    <aside className={styles.nav} aria-label="사이드바">
      <Link href="/main" className={styles.logo} aria-label="찍먹 dipsource 홈">
        <span className={styles.mark} aria-hidden="true">
          <span className={styles.bite} />
        </span>
        <span className={styles.brand}>
          <strong>찍먹</strong>
          <span>dipsource</span>
        </span>
      </Link>
      <NavLinks />
      <section className={styles.ranking} aria-labelledby="ranking-title">
        <Suspense fallback={<p className={styles.loading}>불러오는 중…</p>}>
          <RankingWidget />
        </Suspense>
      </section>
      <p className={styles.copy}>© 2025 찍먹 dipsource</p>
    </aside>
  );
}
