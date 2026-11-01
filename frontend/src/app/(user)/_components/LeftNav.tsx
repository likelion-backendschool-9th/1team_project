import { Suspense } from "react";
import { Brand } from "./Brand";
import { NavLinks } from "./NavLinks";
import { RankingWidget } from "./RankingWidget";
import styles from "./LeftNav.module.css";

export function LeftNav() {
  return (
    <aside className={styles.nav} aria-label="사이드바">
      <Brand />
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
