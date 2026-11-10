import Link from "next/link";
import { connection } from "next/server";
import { Avatar } from "@/app/_components/ui/Avatar";
import kit from "@/app/_components/ui/kit.module.css";
import { LinkTabs } from "@/app/_components/ui/LinkTabs";
import { formatCount } from "@/app/_lib/format";
import type { RankingPeriod } from "@/app/_lib/types/user";
import { getRankings, type RankingType } from "../_lib/api";
import styles from "./Rankings.module.css";

type Props = {
  searchParams: Promise<Record<string, string | string[] | undefined>>;
};

const types: { value: RankingType; label: string; unit: string }[] = [
  { value: "demo", label: "🧪 데모", unit: "체험" },
  { value: "blog", label: "📝 블로그", unit: "조회" },
  { value: "follower", label: "👥 팔로워", unit: "명" },
  { value: "home", label: "🏠 미니홈피", unit: "방문" },
];

const periods: { value: RankingPeriod; label: string }[] = [
  { value: "daily", label: "오늘" },
  { value: "weekly", label: "이번주" },
  { value: "monthly", label: "이번달" },
];

function first(value: string | string[] | undefined) {
  return Array.isArray(value) ? value[0] : value;
}

function href(type: RankingType, period: RankingPeriod) {
  const query = new URLSearchParams();
  if (type !== "demo") query.set("type", type);
  if (period !== "weekly") query.set("period", period);
  const search = query.toString();
  return search ? `/rankings?${search}` : "/rankings";
}

export async function RankingContent({ searchParams }: Props) {
  await connection();
  const params = await searchParams;
  const type = types.find((item) => item.value === first(params.type)) ?? types[0];
  const period = periods.find((item) => item.value === first(params.period))?.value ?? "weekly";
  const rows = await getRankings(type.value, period);
  const podium = rows.slice(0, 3);
  const rest = rows.slice(3);
  const showTitle = type.value === "demo" || type.value === "blog";

  return (
    <div className={kit.stack}>
      <div className={kit.between}>
        <LinkTabs
          label="랭킹 종류"
          items={types.map((item) => ({ href: href(item.value, period), label: item.label, active: item.value === type.value }))}
        />
        <LinkTabs
          label="기간"
          variant="line"
          items={periods.map((item) => ({ href: href(type.value, item.value), label: item.label, active: item.value === period }))}
        />
      </div>
      <ol className={styles.podium}>
        {podium.map((row) => (
          <li key={row.rank} className={`${styles.place} ${styles[`place${row.rank}`]}`}>
            <span className={styles.medal} aria-hidden="true">
              {["🥇", "🥈", "🥉"][row.rank - 1]}
            </span>
            <span className="sr-only">{row.rank}위</span>
            <Link href={`/mini-home/${row.user.id}`} className={styles.placeUser}>
              <Avatar name={row.user.nickname} size={row.rank === 1 ? 64 : 52} />
              <strong>{row.user.nickname}</strong>
            </Link>
            {showTitle && <span className={styles.placeTitle}>{row.title}</span>}
            <span className={styles.score}>
              {formatCount(row.score)} {type.unit}
            </span>
          </li>
        ))}
      </ol>
      <section className={kit.card} aria-label="4위 이하">
        <ol className={kit.list} start={4}>
          {rest.map((row) => (
            <li key={row.rank} className={kit.listItem}>
              <span className={styles.rank}>{row.rank}</span>
              <Avatar name={row.user.nickname} size={36} />
              <span className={kit.grow}>
                <Link href={`/mini-home/${row.user.id}`} className={kit.strong}>
                  {row.user.nickname}
                </Link>
                {showTitle && <span className={kit.faint}> · {row.title}</span>}
              </span>
              <span className={styles.change} data-trend={row.change > 0 ? "up" : row.change < 0 ? "down" : "same"}>
                {row.change > 0 ? `▲ ${row.change}` : row.change < 0 ? `▼ ${-row.change}` : "–"}
                <span className="sr-only">
                  {row.change > 0 ? "계단 상승" : row.change < 0 ? "계단 하락" : "변동 없음"}
                </span>
              </span>
              <span className={styles.scoreRow}>
                {formatCount(row.score)} {type.unit}
              </span>
            </li>
          ))}
        </ol>
      </section>
    </div>
  );
}
