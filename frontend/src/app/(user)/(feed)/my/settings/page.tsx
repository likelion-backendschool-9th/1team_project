import type { Metadata } from "next";
import { Suspense } from "react";
import { Avatar } from "@/app/_components/ui/Avatar";
import { ConfirmButton } from "@/app/_components/ui/ConfirmButton";
import kit from "@/app/_components/ui/kit.module.css";
import { formatDate } from "@/app/_lib/format";
import { unblockUser } from "../_actions";
import { getBlocked, getSettings } from "../_lib/api";
import { SettingsForm } from "./_components/SettingsForm";

export const metadata: Metadata = { title: "설정", robots: { index: false } };

async function SettingsContent() {
  const [settings, blocked] = await Promise.all([getSettings(), getBlocked()]);

  return (
    <>
      <SettingsForm settings={settings} />
      <section className={kit.card} aria-labelledby="blocked-title">
        <h2 id="blocked-title" className={kit.cardTitle}>
          차단한 사용자
        </h2>
        {blocked.length === 0 ? (
          <p className={kit.empty}>차단한 사용자가 없어요.</p>
        ) : (
          <ul className={kit.list}>
            {blocked.map((user) => (
              <li key={user.id} className={kit.listItem}>
                <Avatar name={user.nickname} size={36} tone="light" />
                <span className={kit.grow}>
                  <strong>{user.nickname}</strong>
                  <span className={kit.faint}> · {formatDate(user.blockedAt)} 차단</span>
                </span>
                <ConfirmButton
                  label="차단 해제"
                  title={`${user.nickname}님 차단을 해제할까요?`}
                  confirmLabel="해제"
                  className={`${kit.btnGhost} ${kit.sm}`}
                  onConfirm={unblockUser.bind(null, user.id)}
                />
              </li>
            ))}
          </ul>
        )}
      </section>
    </>
  );
}

export default function SettingsPage() {
  return (
    <>
      <h1 className={kit.pageTitle}>설정</h1>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <SettingsContent />
      </Suspense>
    </>
  );
}
