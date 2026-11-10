import { ConfirmButton } from "@/app/_components/ui/ConfirmButton";
import kit from "@/app/_components/ui/kit.module.css";
import type { OAuthProvider } from "@/app/_lib/types/auth";
import { unlinkSocialAccount } from "../_actions";

const providers: { value: OAuthProvider; label: string }[] = [
  { value: "github", label: "GitHub" },
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

  return (
    <section className={kit.card} aria-labelledby="social-title">
      <h2 id="social-title" className={kit.cardTitle}>
        소셜 계정 연동
      </h2>
      <p className={kit.faint}>연동해 두면 어떤 소셜 계정으로 로그인해도 이 계정으로 들어와요.</p>

      {justLinkedLabel && (
        <p role="status" className={kit.pillSuccess} style={{ marginTop: 12 }}>
          {justLinkedLabel} 계정을 연동했어요.
        </p>
      )}
      {linkError && (
        <p role="alert" className={kit.error} style={{ marginTop: 12 }}>
          {linkErrors[linkError] ?? "연동하지 못했어요. 잠시 후 다시 시도해 주세요."}
        </p>
      )}

      <ul className={kit.list} style={{ marginTop: 12 }}>
        {providers.map((provider) => {
          const isLinked = linked.includes(provider.value);
          return (
            <li key={provider.value} className={kit.listItem}>
              <span className={kit.grow}>
                <strong>{provider.label}</strong>
              </span>
              {isLinked ? (
                <>
                  <span className={kit.pillSuccess}>연동됨</span>
                  {linked.length > 1 && (
                    <ConfirmButton
                      label="해제"
                      title={`${provider.label} 연동을 해제할까요?`}
                      description="해제하면 이 소셜 계정으로는 로그인할 수 없어요."
                      confirmLabel="해제"
                      danger
                      className={`${kit.btnGhost} ${kit.sm}`}
                      onConfirm={unlinkSocialAccount.bind(null, provider.value)}
                    />
                  )}
                </>
              ) : (
                // 백엔드 OAuth로 이동하는 주소라 Next Link가 아니라 a를 쓴다
                <a href={`${browserApiOrigin}/api/v1/auth/link/${provider.value}`} className={`${kit.btn} ${kit.sm}`}>
                  연동하기
                </a>
              )}
            </li>
          );
        })}
      </ul>
    </section>
  );
}