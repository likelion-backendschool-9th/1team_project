import { connection } from "next/server";
import { LinkTabs } from "@/app/_components/ui/LinkTabs";
import { getNotifications, getServerTime } from "../_lib/api";
import { NotificationList } from "./NotificationList";

type Props = {
  searchParams: Promise<Record<string, string | string[] | undefined>>;
};

export async function NotificationContent({ searchParams }: Props) {
  await connection();
  const unreadOnly = (await searchParams).filter === "unread";
  const [notifications, now] = await Promise.all([getNotifications(), getServerTime()]);
  const unreadCount = notifications.filter((item) => !item.read).length;

  return (
    <>
      <LinkTabs
        label="알림 필터"
        variant="line"
        items={[
          { href: "/notifications", label: "전체", active: !unreadOnly, count: notifications.length },
          { href: "/notifications?filter=unread", label: "안 읽음", active: unreadOnly, count: unreadCount },
        ]}
      />
      <NotificationList
        key={unreadOnly ? "unread" : "all"}
        notifications={unreadOnly ? notifications.filter((item) => !item.read) : notifications}
        now={now}
      />
    </>
  );
}
