import Link from "next/link";
import styles from "./LinkTabs.module.css";

type Item = {
  href: string;
  label: string;
  active: boolean;
  count?: number;
};

type Props = {
  label: string;
  items: Item[];
  variant?: "chip" | "line";
};

// URL로 상태를 바꾸는 탭. 공유·뒤로 가기가 되도록 링크로 만든다.
export function LinkTabs({ label, items, variant = "chip" }: Props) {
  return (
    <nav aria-label={label}>
      <ul className={`${styles.list} ${styles[variant]}`}>
        {items.map((item) => (
          <li key={item.href}>
            <Link
              href={item.href}
              className={`${styles.tab} ${item.active ? styles.active : ""}`}
              aria-current={item.active ? "page" : undefined}
            >
              {item.label}
              {item.count !== undefined && <span className={styles.count}>{item.count}</span>}
            </Link>
          </li>
        ))}
      </ul>
    </nav>
  );
}
