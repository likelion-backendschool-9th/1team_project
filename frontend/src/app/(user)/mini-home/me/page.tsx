import { redirect } from "next/navigation";
import { connection } from "next/server";
import { getMe } from "@/app/(user)/_lib/api";

// "내 미니홈피" 바로가기. 로그인한 사람의 홈피로 보내고, 비로그인이면 로그인 후 다시 돌아오게 한다.
export default async function MyMiniHomePage() {
  await connection();
  const me = await getMe();
  if (!me) redirect(`/login?redirect=${encodeURIComponent("/mini-home/me")}`);
  redirect(`/mini-home/${me.id}`);
}
