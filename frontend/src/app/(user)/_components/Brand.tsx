import Link from "next/link";
import styles from "./Brand.module.css";

type Props = {
  caps?: boolean;
};

export function Brand({ caps = false }: Props) {
  return (
    <Link href="/main" className={styles.logo} aria-label="찍먹 dipsource 홈">
      <span className={styles.mark} aria-hidden="true">
        <span className={styles.bite} />
      </span>
      <span className={`${styles.brand} ${caps ? styles.caps : ""}`}>
        <strong>찍먹</strong>
        <span>dipsource</span>
      </span>
    </Link>
  );
}
