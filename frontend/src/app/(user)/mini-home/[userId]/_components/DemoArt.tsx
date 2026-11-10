import styles from "./DemoArt.module.css";

type Props = {
  variant: number;
  caption: string;
  className?: string;
  thumbnail?: string | null;
};

const tones = [styles.tone0, styles.tone1, styles.tone2, styles.tone3];

export function DemoArt({ variant, caption, className = "", thumbnail }: Props) {
  if (thumbnail)
    return (
      <div
        className={`${styles.art} ${styles.image} ${className}`}
        style={{ backgroundImage: `url("${thumbnail}")` }}
      />
    );

  return (
    <div className={`${styles.art} ${tones[variant % 4]} ${className}`}>
      <span className={styles.glyph} aria-hidden="true">
        {"</>"}
      </span>
      {caption ? <span className={styles.label}>{caption}</span> : null}
    </div>
  );
}
