import type { Metadata } from "next";
import Link from "next/link";
import { redirect } from "next/navigation";
import { connection } from "next/server";
import { Suspense } from "react";
import { hasBackend } from "@/app/_lib/api/client";
import { DEV_LOGIN_ACCOUNTS } from "./_constants";
import { loginAsDevAccount } from "./_actions";

export const metadata: Metadata = {
  title: "로컬 시드 로그인 (임시)",
  robots: { index: false, follow: false },
};

/**
 * TEMP-DEV-LOGIN: 시드 계정 클릭 로그인.
 * 삭제 시 이 폴더(login/dev) + 로그인 페이지 링크 + 백엔드 DevLoginController 를 함께 지운다.
 */

async function ErrorBanner({ searchParams }: { searchParams: Promise<{ error?: string }> }) {
  await connection();
  const error = (await searchParams).error;
  if (!error) return null;
  return (
    <p role="alert" style={{ color: "#b91c1c", marginBottom: 12 }}>
      로그인 실패 (HTTP {error}). 시드·DEV_LOGIN_ENABLED·백엔드 재시작을 확인하세요.
    </p>
  );
}

export default function DevLoginPage({
  searchParams,
}: {
  searchParams: Promise<{ error?: string }>;
}) {
  if (!hasBackend) {
    redirect("/login");
  }

  return (
    <main style={{ maxWidth: 480, margin: "40px auto", padding: 16, fontFamily: "sans-serif" }}>
      <p style={{ color: "#b45309", marginBottom: 8 }}>
        TEMP · 로컬 전용. 나중에 삭제하세요. (TEMP-DEV-LOGIN)
      </p>
      <h1 style={{ fontSize: 22, marginBottom: 8 }}>시드 계정 로그인</h1>
      <p style={{ marginBottom: 16, color: "#444" }}>
        DB에 시드가 있어야 합니다. <code>backend/scripts/dev-seed/./up.sh</code>
        <br />
        백엔드에 <code>DEV_LOGIN_ENABLED=true</code> 후 재시작.
      </p>
      <Suspense>
        <ErrorBanner searchParams={searchParams} />
      </Suspense>
      <ul style={{ listStyle: "none", padding: 0, margin: 0, display: "grid", gap: 8 }}>
        {DEV_LOGIN_ACCOUNTS.map((account) => (
          <li key={account.email}>
            <form action={loginAsDevAccount}>
              <input type="hidden" name="email" value={account.email} />
              <button
                type="submit"
                style={{
                  width: "100%",
                  textAlign: "left",
                  padding: "12px 14px",
                  cursor: "pointer",
                  border: "1px solid #ccc",
                  borderRadius: 6,
                  background: "#fff",
                }}
              >
                <strong>{account.label}</strong>
                <br />
                <span style={{ fontSize: 13, color: "#666" }}>
                  {account.email} · {account.note}
                </span>
              </button>
            </form>
          </li>
        ))}
      </ul>
      <p style={{ marginTop: 20 }}>
        <Link href="/login">← 소셜 로그인으로</Link>
      </p>
    </main>
  );
}
