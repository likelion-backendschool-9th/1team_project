import type { UserBadge } from "@/app/_lib/types/user";
import styles from "./UserBadges.module.css";

const MAX_BADGES = 3;

type Props = {
  badges: UserBadge[];
  align?: "start" | "center";
  size?: "md" | "sm";
};

export function UserBadges({ badges, align = "start", size = "md" }: Props) {
  if (badges.length === 0) return null;

  return (
    <ul
      className={`${styles.list} ${align === "center" ? styles.center : ""} ${size === "sm" ? styles.sm : ""}`}
      aria-label="배지"
    >
      {badges.slice(0, MAX_BADGES).map((badge) => (
        <li key={badge.id} className={`${styles.badge} ${styles[badge.tone]}`}>
          {badge.icon && (
            <span className={styles.icon} aria-hidden="true">
              {badge.icon}
            </span>
          )}
          {badge.label}
        </li>
      ))}
    </ul>
  );
}
