import { ConfirmButton } from "@/app/_components/ui/ConfirmButton";
import kit from "@/app/_components/ui/kit.module.css";
import { ProviderIcon } from "@/app/_components/ui/ProviderIcon";
import type { OAuthProvider } from "@/app/_lib/types/auth";
import { unlinkSocialAccount } from "../_actions";
import styles from "./SocialAccounts.module.css";

const providers: { value: OAuthProvider; label: string }[] = [
  { value: "github", label: "깃허브" },
  { value: "google", label: "구글" },
  { value: "kakao", label: "카카오" },
];

const linkErrors: Record<string, string> = {
  conflict: "이미 다른 찍먹 계정에 연결된 소셜 계정이라 연동하지 못했어요.",
  bad_request: "이미 같은 종류의 소셜 계정이 연결돼 있어요.",
};

type Props = {
  linked: OAuthProvider[];
  /** 방금 연동에 성공한 소셜 (주소의 ?linked=) */
  justLinked?: string;
  /** 연동 실패 이유 (주소의 ?linkError=) */
  linkError?: string;
};

export function SocialAccounts({ linked, justLinked, linkError }: Props) {
  // 연동은 백엔드 OAuth로 브라우저가 직접 이동한다. 로그인 버튼과 같은 주소 규칙을 쓴다
  const browserApiOrigin = process.env.BROWSER_API_ORIGIN ?? process.env.API_ORIGIN ?? "";
  const justLinkedLabel = providers.find((item) => item.value === justLinked)?.label;
  const lastOne = linked.length <= 1;

  return (
    <section className={`${kit.card} ${styles.section}`} aria-labelledby="social-title">
      <div className={styles.intro}>
        <h2 id="social-title" className={kit.cardTitle}>
          연동된 계정
        </h2>
        <p className={kit.faint}>연동해 두면 어떤 소셜 계정으로 로그인해도 이 계정으로 들어와요.</p>
      </div>

      <div className={styles.body}>
        {justLinkedLabel && (
          <p role="status" className={styles.noticeOk}>
            {justLinkedLabel} 계정을 연동했어요
          </p>
        )}
        {linkError && (
          <p role="alert" className={styles.noticeError}>
            {linkErrors[linkError] ?? "연동하지 못했어요. 잠시 후 다시 시도해 주세요."}
          </p>
        )}

        <ul className={styles.cards}>
          {providers.map((provider) => {
            const isLinked = linked.includes(provider.value);
            return (
              <li key={provider.value} className={`${styles.card} ${isLinked ? styles.linked : ""}`}>
                <ProviderIcon provider={provider.value} size={24} />
                <p className={styles.name}>
                  {provider.label}
                  {isLinked && <span className={styles.badge}>연동됨</span>}
                </p>
                <p className={styles.status}>
                  {isLinked ? (lastOne ? "유일한 로그인 수단" : "로그인에 사용 중") : "연동 안 됨"}
                </p>

                {isLinked ? (
                  lastOne ? (
                    // 소셜 로그인만 쓰므로 마지막 하나를 끊으면 로그인할 방법이 없어진다
                    <span className={`${styles.action} ${styles.actionDisabled}`} aria-disabled="true">
                      해제 불가
                    </span>
                  ) : (
                    <ConfirmButton
                      label="해제"
                      title={`${provider.label} 연동을 해제할까요?`}
                      description="해제하면 이 소셜 계정으로는 로그인할 수 없어요."
                      confirmLabel="해제"
                      danger
                      className={`${styles.action} ${styles.actionGhost}`}
                      onConfirm={unlinkSocialAccount.bind(null, provider.value)}
                    />
                  )
                ) : (
                  // 백엔드 OAuth로 이동하는 주소라 Next Link가 아니라 a를 쓴다
                  <a
                    href={`${browserApiOrigin}/api/v1/auth/link/${provider.value}`}
                    className={`${styles.action} ${styles.actionPrimary}`}
                  >
                    연동하기
                  </a>
                )}
              </li>
            );
          })}
        </ul>
      </div>
    </section>
  );
}
