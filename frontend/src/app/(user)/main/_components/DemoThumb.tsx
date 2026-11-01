import styles from "./DemoThumb.module.css";

type Props = {
  seed: number;
  thumbnail: string | null;
  label?: string;
  className?: string;
};

export function DemoThumb({ seed, thumbnail, label, className = "" }: Props) {
  return (
    <div
      className={`${styles.thumb} ${styles[`tone${seed % 4}`]} ${className}`}
      style={thumbnail ? { backgroundImage: `url(${thumbnail})` } : undefined}
    >
      {label && <span className={styles.label}>{label}</span>}
    </div>
  );
}
