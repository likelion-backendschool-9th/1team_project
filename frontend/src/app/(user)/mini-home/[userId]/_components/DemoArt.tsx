import styles from "./DemoArt.module.css";

type Props = {
  variant: number;
  caption: string;
  className?: string;
  thumbnail?: string | null;
};

// 썸네일이 없는 데모를 위한 기본 그림. 예전 목업처럼 보이지 않도록 실행 환경 이름만 담백하게 보여 준다.
// variant는 예전 호출부와 맞추려고 남겨 둔 값이라 쓰지 않는다.
export function DemoArt({ caption, className = "", thumbnail }: Props) {
  if (thumbnail)
    return (
      <div
        className={`${styles.art} ${styles.image} ${className}`}
        style={{ backgroundImage: `url("${thumbnail}")` }}
      />
    );

  return (
    <div className={`${styles.art} ${styles.glass} ${className}`}>
      <span className={styles.pill}>{caption}</span>
    </div>
  );
}
