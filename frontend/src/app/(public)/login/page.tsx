import type { Metadata } from "next";
import { Suspense } from "react";
import { Brand } from "@/app/(user)/_components/Brand";
import { oauthProviders, type OAuthProvider } from "@/app/_lib/types/auth";
import { parseRedirect } from "@/app/_lib/redirect";
import { startLogin } from "./_actions";
import { ProviderIcon } from "./_components/ProviderIcon";
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
          <ProviderList searchParams={searchParams} />
        </Suspense>
        <p className={styles.terms}>
          계속하면 찍먹의 이용약관과 개인정보 처리방침에 동의하는 것으로 간주돼요.
        </p>
      </section>
    </main>
  );
}
