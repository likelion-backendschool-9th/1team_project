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
      style={thumbnail ? { backgroundImage: `url("${thumbnail}")` } : undefined}
    >
      {/* 썸네일이 없으면 단색 상자 대신 코드 모양 그림을 은은하게 깔아 준다 (장식) */}
      {!thumbnail && (
        <span className={styles.glyph} aria-hidden="true">
          {"</>"}
        </span>
      )}
      {label && <span className={styles.label}>{label}</span>}
    </div>
  );
}
