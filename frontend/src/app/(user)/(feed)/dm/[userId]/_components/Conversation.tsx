import Link from "next/link";
import { notFound } from "next/navigation";
import { connection } from "next/server";
import { Avatar } from "@/app/_components/ui/Avatar";
import { getMe } from "@/app/(user)/_lib/api";
import { getMessages, getRooms } from "../../_lib/api";
import { ChatPanel } from "./ChatPanel";
import styles from "../../_components/Dm.module.css";

type Props = {
  params: Promise<{ userId: string }>;
};

export async function Conversation({ params }: Props) {
  await connection();
  const partnerId = Number((await params).userId);
  const [me, rooms] = await Promise.all([getMe(), getRooms()]);
  const room = rooms.find((item) => item.user.id === partnerId);
  if (!room) notFound();

  const messages = await getMessages(me.id, partnerId);

  return (
    <>
      <header className={styles.chatHeader}>
        <Avatar name={room.user.nickname} size={40} />
        <div>
          <h2 className={styles.chatName}>{room.user.nickname}</h2>
          <p className={styles.chatSub}>{room.online ? "접속 중" : "오프라인"}</p>
        </div>
        <Link href={`/mini-home/${room.user.id}`} className={styles.homeLink}>
          미니홈피
        </Link>
      </header>
      <ChatPanel myId={me.id} initialMessages={messages} partnerName={room.user.nickname} />
    </>
  );
}
