import type { Metadata } from "next";
import Link from "next/link";
import { Suspense } from "react";
import { Brand } from "@/app/(user)/_components/Brand";
import { oauthProviders, type OAuthProvider } from "@/app/_lib/types/auth";
import { parseRedirect } from "@/app/_lib/redirect";
import { startLogin } from "./_actions";
import { ProviderIcon } from "@/app/_components/ui/ProviderIcon";
import styles from "./page.module.css";


export const metadata: Metadata = {
  title: "로그인",
  description: "GitHub, 카카오, 구글 계정으로 찍먹을 시작하세요.",
  alternates: { canonical: "/login" },
};

const providerLabels: Record<OAuthProvider, string> = {
  github: "GitHub로 계속하기",
  kakao: "카카오로 계속하기",
  google: "구글로 계속하기",
};

const loginErrors: Record<string, string> = {
  suspended: "이용이 정지된 계정이에요.",
  deleted: "탈퇴한 계정이에요.",
};

async function LoginError({ searchParams }: Pick<PageProps<"/login">, "searchParams">) {
  const { error } = await searchParams;
  const message = typeof error === "string" ? loginErrors[error] : undefined;
  if (!message) return null;
  return (
    <p role="alert" className={styles.error}>
      {message}
      </p>
  );
}

async function ProviderList({ searchParams }: Pick<PageProps<"/login">, "searchParams">) {
  const redirectTo = parseRedirect((await searchParams).redirect);

  return (
    <ul className={styles.list}>
      {oauthProviders.map((provider) => (
        <li key={provider}>
          <form action={startLogin}>
            <input type="hidden" name="provider" value={provider} />
            <input type="hidden" name="redirect" value={redirectTo} />
            <button type="submit" className={`${styles.button} ${styles[provider]}`}>
              <ProviderIcon provider={provider} />
              {providerLabels[provider]}
            </button>
          </form>
        </li>
      ))}
    </ul>
  );
}

export default function LoginPage({ searchParams }: PageProps<"/login">) {
  return (
    <main id="main-content" tabIndex={-1} className={styles.page}>
      <section className={styles.card} aria-labelledby="login-title">
        <Brand />
        <h1 id="login-title" className={styles.title}>
          로그인하고 찍먹 시작하기
        </h1>
        <p className={styles.desc}>별도 가입 없이 소셜 계정으로 바로 시작할 수 있어요.</p>
        <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
          <LoginError searchParams={searchParams} />
          <ProviderList searchParams={searchParams} />
        </Suspense>
        {process.env.DEV_LOGIN_ENABLED === "true" && (
          <Link href="/login/dev" className={styles.button} style={{ marginTop: 8, textAlign: "center", background: "#f3f4f6", color: "#374151" }}>
            테스트 계정으로 로그인
          </Link>
        )}
        <p className={styles.terms}>
          계속하면 찍먹의 이용약관과 개인정보 처리방침에 동의하는 것으로 간주돼요.
        </p>
      </section>
    </main>
  );
}
