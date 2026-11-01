import Link from "next/link";
import { Avatar } from "@/app/_components/ui/Avatar";
import { ChevronRightIcon } from "@/app/_components/ui/Icons";
import { formatCount } from "@/app/_lib/format";
import { getWeeklyRanking } from "../_lib/api";
import styles from "./RankingWidget.module.css";

export async function RankingWidget() {
  const ranking = await getWeeklyRanking();

  return (
    <>
      <div className={styles.header}>
        <h2 id="ranking-title" className={styles.title}>
          오늘의 개발자
        </h2>
        <Link href="/rankings?type=follower&period=weekly" className={styles.more}>
          이번 주
          <ChevronRightIcon />
        </Link>
      </div>
      <ol className={styles.list}>
        {ranking.map(({ rank, user, score }) => (
          <li key={user.id}>
            <Link href={`/mini-home/${user.id}`} className={styles.row}>
              <span className={`${styles.rank} ${rank === 1 ? styles.first : ""}`}>
                {rank}
              </span>
              <Avatar name={user.nickname} size={40} />
              <span className={styles.name}>{user.nickname}</span>
              <span className={styles.score}>{formatCount(score)}</span>
            </Link>
          </li>
        ))}
      </ol>
    </>
  );
}
