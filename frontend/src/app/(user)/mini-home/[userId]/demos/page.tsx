import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { connection } from "next/server";
import { Suspense } from "react";
import { ConfirmButton } from "@/app/_components/ui/ConfirmButton";
import kit from "@/app/_components/ui/kit.module.css";
import { formatWon } from "@/app/_lib/format";
import {
  chipsToWon,
  INVENTORY_SLOT_CHIPS,
  inventoryCapacity,
  showcaseLimit,
} from "@/app/_lib/policy";
import { getMe } from "@/app/(user)/_lib/api";
import { expandInventory } from "../_actions";
import { PublicDemos } from "../_components/PublicDemos";
import { getDemos, getProfile } from "../_lib/api";
import { parseUserId } from "../_lib/parse";
import styles from "./Inventory.module.css";

export const metadata: Metadata = { title: "미니홈피 데모" };

async function DemosContent({ params }: { params: Promise<{ userId: string }> }) {
  await connection();
  const userId = parseUserId((await params).userId);
  const profile = userId ? await getProfile(userId) : null;
  if (!userId || !profile) notFound();
  const [me, demos] = await Promise.all([getMe(), getDemos(userId, 20)]);
  const capacity = inventoryCapacity(profile.plus, profile.extraInventorySlots);
  const shown = profile.showcaseDemoIds.slice(0, showcaseLimit(profile.plus));
  const isOwner = me?.id === userId;

  return (
    <>
      <section className={`${kit.card} ${styles.meter}`} aria-label="데모 인벤토리">
        <div className={kit.grow}>
          <p className={kit.strong}>
            데모 인벤토리 {demos.length}/{capacity}칸
            {profile.plus && <span className={kit.pillAccent} style={{ marginLeft: 8 }}>PLUS +2칸</span>}
          </p>
          <div className={styles.slots} aria-hidden="true">
            {Array.from({ length: capacity }, (_, slot) => slot + 1).map((slot) => (
              <span key={slot} className={slot <= demos.length ? styles.filled : undefined} />
            ))}
          </div>
          <p className={kit.faint}>
            미니홈피 공개 {shown.length}/{showcaseLimit(profile.plus)}개
            {!profile.plus && " · 딥소스 플러스면 6개까지 공개할 수 있어요"}
          </p>
        </div>
        {isOwner && (
          <div className={kit.row}>
            <Link href="/demos/upload" className={kit.btnPrimary}>
              + 데모 올리기
            </Link>
            <ConfirmButton
              label={`+1칸 늘리기 (${INVENTORY_SLOT_CHIPS}칩)`}
              title="데모 인벤토리를 1칸 늘릴까요?"
              description={`${INVENTORY_SLOT_CHIPS}칩(${formatWon(chipsToWon(INVENTORY_SLOT_CHIPS))})이 차감돼요.`}
              confirmLabel="늘리기"
              className={kit.btn}
              onConfirm={expandInventory}
            />
          </div>
        )}
      </section>
      <PublicDemos
        userId={userId}
        demos={demos}
        featuredId={profile.featuredDemoId ?? undefined}
        showcaseIds={shown}
        title={isOwner ? "내 데모 인벤토리" : `${profile.nickname}님의 데모`}
        showMore={false}
      />
    </>
  );
}

export default function MiniHomeDemosPage({ params }: PageProps<"/mini-home/[userId]/demos">) {
  return (
    <>
      <h1 className="sr-only">미니홈피 데모 목록</h1>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <DemosContent params={params} />
      </Suspense>
    </>
  );
}
