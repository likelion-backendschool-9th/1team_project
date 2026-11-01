import type { Metadata } from "next";
import Link from "next/link";
import { connection } from "next/server";
import { Suspense } from "react";
import kit from "@/app/_components/ui/kit.module.css";
import { LinkTabs } from "@/app/_components/ui/LinkTabs";
import { formatDate } from "@/app/_lib/format";
import type { EventStatus } from "@/app/_lib/types/event";
import { statusLabels } from "./_components/eventLabels";
import { getEvents } from "./_lib/api";
import styles from "./_components/Events.module.css";

export const metadata: Metadata = {
  title: "이벤트",
  description: "찍먹 챌린지와 이벤트에 참여하고 칩과 배지를 받아 보세요.",
};

const statuses = Object.keys(statusLabels) as EventStatus[];

async function EventList({ searchParams }: { searchParams: Promise<Record<string, string | string[] | undefined>> }) {
  await connection();
  const raw = (await searchParams).status;
  const status = statuses.find((item) => item === raw);
  const events = await getEvents(status);

  return (
    <>
      <LinkTabs
        label="이벤트 상태"
        items={[
          { href: "/events", label: "전체", active: !status },
          ...statuses.map((item) => ({ href: `/events?status=${item}`, label: statusLabels[item], active: item === status })),
        ]}
      />
      {events.length === 0 ? (
        <p className={kit.empty}>해당하는 이벤트가 없어요.</p>
      ) : (
        <ul className={styles.grid}>
          {events.map((event) => (
            <li key={event.id}>
              <Link href={`/events/${event.id}`} className={`${styles.card} ${event.status === "ended" ? styles.ended : ""}`}>
                <span className={`${styles.banner} ${styles[event.status]}`} aria-hidden="true">
                  {event.emoji}
                </span>
                <span className={styles.body}>
                  <span className={event.status === "ongoing" ? kit.pillSuccess : event.status === "upcoming" ? kit.pillWarn : kit.pill}>
                    {statusLabels[event.status]}
                  </span>
                  <span className={styles.title}>{event.title}</span>
                  <span className={kit.muted}>{event.summary}</span>
                  <span className={kit.faint}>
                    {formatDate(event.startAt)} ~ {formatDate(event.endAt)} · 참가 {event.participantCount}명
                  </span>
                  <span className={styles.reward}>🎁 {event.reward}</span>
                </span>
              </Link>
            </li>
          ))}
        </ul>
      )}
    </>
  );
}

export default function EventsPage({ searchParams }: PageProps<"/events">) {
  return (
    <>
      <header>
        <h1 className={kit.pageTitle}>이벤트</h1>
        <p className={kit.pageDesc}>챌린지에 참여하고 칩과 한정 배지를 받아 보세요.</p>
      </header>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <EventList searchParams={searchParams} />
      </Suspense>
    </>
  );
}
