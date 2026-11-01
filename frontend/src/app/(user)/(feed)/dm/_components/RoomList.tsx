import { connection } from "next/server";
import { getRooms, getServerTime } from "../_lib/api";
import { RoomLinks } from "./RoomLinks";

export async function RoomList() {
  await connection();
  const [rooms, now] = await Promise.all([getRooms(), getServerTime()]);
  return <RoomLinks rooms={rooms} now={now} />;
}
