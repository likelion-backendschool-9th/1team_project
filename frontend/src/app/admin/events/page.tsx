import type { Metadata } from "next";
import Link from "next/link";
import { connection } from "next/server";
import { Suspense } from "react";
import { ConfirmButton } from "@/app/_components/ui/ConfirmButton";
import kit from "@/app/_components/ui/kit.module.css";
import { formatDate } from "@/app/_lib/format";
import { deleteEvent, saveEvent } from "../_actions";
import { StatusPill } from "../_components/StatusPill";
import { getAdminEvents } from "../_lib/api";
import { EventForm } from "./_components/EventForm";

export const metadata: Metadata = { title: "이벤트 관리" };

async function EventsContent({ searchParams }: { searchParams: Promise<Record<string, string | string[] | undefined>> }) {
  await connection();
  const editId = Number((await searchParams).edit);
  const events = await getAdminEvents();
  const editing = events.find((event) => event.id === editId) ?? null;

  return (
    <>
      <EventForm
        key={editing?.id ?? "new"}
        action={saveEvent.bind(null, editing?.id ?? null)}
        event={editing}
      />
      <section className={kit.card}>
        <div className={kit.tableWrap}>
          <table className={kit.table}>
            <thead>
              <tr>
                <th scope="col">ID</th>
                <th scope="col">제목</th>
                <th scope="col">기간</th>
                <th scope="col" className={kit.num}>참가</th>
                <th scope="col">상태</th>
                <th scope="col"><span className="sr-only">관리</span></th>
              </tr>
            </thead>
            <tbody>
              {events.map((event) => (
                <tr key={event.id}>
                  <td>{event.id}</td>
                  <td className={kit.strong}>
                    <span aria-hidden="true">{event.emoji}</span> {event.title}
                  </td>
                  <td>
                    {formatDate(event.startAt)} ~ {formatDate(event.endAt)}
                  </td>
                  <td className={kit.num}>{event.participantCount}</td>
                  <td>
                    <StatusPill status={event.status} />
                  </td>
                  <td>
                    <div className={kit.row}>
                      <Link href={`/admin/events?edit=${event.id}`} className={`${kit.btnGhost} ${kit.sm}`}>
                        수정
                      </Link>
                      <ConfirmButton
                        label="삭제"
                        title="이벤트를 삭제할까요?"
                        description={`「${event.title}」 참가 기록도 함께 숨겨져요.`}
                        confirmLabel="삭제"
                        danger
                        className={`${kit.btnGhost} ${kit.sm}`}
                        onConfirm={deleteEvent.bind(null, event.id)}
                      />
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        {events.length === 0 && <p className={kit.empty}>아직 등록한 이벤트가 없어요. 위에서 새 이벤트를 등록해 보세요.</p>}
      </section>
    </>
  );
}

export default function AdminEventsPage({ searchParams }: PageProps<"/admin/events">) {
  return (
    <>
      <h1 className={kit.pageTitle}>이벤트 관리</h1>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <EventsContent searchParams={searchParams} />
      </Suspense>
    </>
  );
}
