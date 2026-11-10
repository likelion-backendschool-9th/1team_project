import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { connection } from "next/server";
import { Suspense } from "react";
import kit from "@/app/_components/ui/kit.module.css";
import { formatCalendarDate } from "@/app/_lib/format";
import { statusLabels } from "../_components/eventLabels";
import { getEvent } from "../_lib/api";
import styles from "../_components/Events.module.css";

export const metadata: Metadata = { title: "이벤트 상세" };

async function EventDetail({ params }: { params: Promise<{ eventId: string }> }) {
  await connection();
  const eventId = Number((await params).eventId);
  const event = Number.isSafeInteger(eventId) && eventId > 0 ? await getEvent(eventId) : null;
  if (!event) notFound();

  return (
    <article className={`${kit.card} ${styles.detail}`} aria-labelledby="event-title">
      {event.imageUrl && (
        // 상세에서는 포스터 글씨가 잘리지 않게 이미지 전체를 보여 준다
        <img src={event.imageUrl} alt={`${event.title} 이벤트 이미지`} className={styles.heroImage} />
      )}
      <div className={`${styles.hero} ${styles[event.status]}`}>
        {!event.imageUrl && <span className={styles.heroEmoji} aria-hidden="true">{event.emoji}</span>}
        <span className={kit.pill}>{statusLabels[event.status]}</span>
        <h2 id="event-title" className={styles.heroTitle}>{event.title}</h2>
        <p>{event.summary}</p>
      </div>
      <dl className={styles.facts}>
        <div><dt>기간</dt><dd>{formatCalendarDate(event.startAt)} ~ {formatCalendarDate(event.endAt)}</dd></div>
        <div><dt>보상</dt><dd>{event.reward}</dd></div>
        {event.participantCount > 0 && <div><dt>참가자</dt><dd>{event.participantCount}명</dd></div>}
      </dl>
      <div className={styles.content}>
        {event.content.split("\n").map((line) => (
          <p key={line}>{line}</p>
        ))}
      </div>
      {event.status === "ongoing" ? (
        <Link href={event.ctaHref} className={`${kit.btnPrimary} ${styles.cta}`}>
          {event.ctaLabel}
        </Link>
      ) : (
        <button type="button" className={`${kit.btnGhost} ${styles.cta}`} disabled>
          {event.status === "upcoming" ? `${formatCalendarDate(event.startAt)}에 시작해요` : "종료된 이벤트예요"}
        </button>
      )}
    </article>
  );
}

export default function EventDetailPage({ params }: PageProps<"/events/[eventId]">) {
  return (
    <>
      <Link href="/events" className={kit.more}>← 이벤트 목록</Link>
      <h1 className="sr-only">이벤트 상세</h1>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <EventDetail params={params} />
      </Suspense>
    </>
  );
}
