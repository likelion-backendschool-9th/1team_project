import type { ReactNode } from "react";
import { Suspense } from "react";
import { Brand } from "../_components/Brand";
import { NavLinks } from "../_components/NavLinks";
import { PresenceHeartbeat } from "../_components/PresenceHeartbeat";
import { ArticleRanking } from "./_components/ArticleRanking";
import { BlogAside } from "./_components/BlogAside";
import styles from "./layout.module.css";

export default function BlogLayout({ children }: { children: ReactNode }) {
  return (
    <div className={styles.shell}>
      <aside className={styles.nav} aria-label="사이드바">
        <Brand />
        <NavLinks />
        <section className={styles.ranking} aria-labelledby="article-ranking-title">
          <Suspense fallback={<p className={styles.loading}>불러오는 중…</p>}>
            <ArticleRanking />
          </Suspense>
        </section>
        <p className={styles.copy}>© 2026 찍먹 dipsource</p>
      </aside>
      <main id="main-content" tabIndex={-1} className={styles.main}>
        {children}
      </main>
      <BlogAside />
      <PresenceHeartbeat />
    </div>
  );
}
