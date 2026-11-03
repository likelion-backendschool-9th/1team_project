import Link from "next/link";
import { notFound } from "next/navigation";
import { connection } from "next/server";
import kit from "@/app/_components/ui/kit.module.css";
import { getMe } from "@/app/(user)/_lib/api";
import { getDemos, getProfile } from "../../_lib/api";
import { parseUserId } from "../../_lib/parse";
import { EditForm } from "./EditForm";

type Props = {
  params: Promise<{ userId: string }>;
};

export async function EditContent({ params }: Props) {
  await connection();
  const userId = parseUserId((await params).userId);
  const profile = userId ? await getProfile(userId) : null;
  if (!userId || !profile) notFound();

  const [me, demos] = await Promise.all([getMe(), getDemos(userId, 20)]);

  if (!me) notFound();
  if (me.id !== userId)
    return (
      <section className={kit.card}>
        <p className={kit.empty}>
          본인 미니홈피만 꾸밀 수 있어요.
          <br />
          <Link href={`/mini-home/${me.id}/edit`} className={kit.more}>
            내 미니홈피 꾸미러 가기 →
          </Link>
        </p>
      </section>
    );

  return <EditForm profile={profile} demos={demos} />;
}
