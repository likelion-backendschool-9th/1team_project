import { Brand } from "./Brand";
import { NavLinks } from "./NavLinks";
import styles from "./LeftNav.module.css";

export function LeftNav() {
  return (
    <aside className={styles.nav} aria-label="사이드바">
      <Brand />
      <NavLinks />
      <p className={styles.copy}>© 2026 찍먹 dipsource</p>
    </aside>
  );
}
