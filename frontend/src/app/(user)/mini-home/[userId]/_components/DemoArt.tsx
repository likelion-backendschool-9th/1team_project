import styles from "./DemoArt.module.css";

type Props = {
  variant: number;
  caption: string;
  className?: string;
};

const NODES = [
  [18, 30], [34, 18], [52, 36], [70, 22], [84, 40], [26, 58],
  [46, 66], [64, 54], [80, 70], [12, 78], [38, 84], [58, 80],
];
const LINKS = [
  [0, 1], [1, 2], [2, 3], [3, 4], [0, 5], [5, 6], [2, 6], [6, 7],
  [7, 4], [7, 8], [5, 9], [9, 10], [10, 6], [10, 11], [11, 8],
];
const BARS = [38, 58, 44, 30, 20];

// 썸네일이 없는 데모를 위한 기본 그림. variant는 목록 순서로 정한다.
export function DemoArt({ variant, caption, className = "" }: Props) {
  if (variant % 3 === 1)
    return (
      <div className={`${styles.art} ${styles.dark} ${className}`}>
        <span className={styles.bars} aria-hidden="true">
          {BARS.map((height) => (
            <span key={height} style={{ height }} />
          ))}
        </span>
        <span className={styles.caption}>{caption}</span>
      </div>
    );

  if (variant % 3 === 2)
    return (
      <div className={`${styles.art} ${styles.glass} ${className}`}>
        <span className={styles.pill}>{caption}</span>
      </div>
    );

  return (
    <div className={`${styles.art} ${styles.dark} ${className}`}>
      <svg className={styles.network} viewBox="0 0 100 100" preserveAspectRatio="none" aria-hidden="true">
        {LINKS.map(([from, to]) => (
          <line
            key={`${from}-${to}`}
            x1={NODES[from][0]}
            y1={NODES[from][1]}
            x2={NODES[to][0]}
            y2={NODES[to][1]}
          />
        ))}
        {NODES.map(([cx, cy]) => (
          <circle key={`${cx}-${cy}`} cx={cx} cy={cy} r="1.6" />
        ))}
      </svg>
      <span className={styles.caption}>{caption}</span>
    </div>
  );
}
