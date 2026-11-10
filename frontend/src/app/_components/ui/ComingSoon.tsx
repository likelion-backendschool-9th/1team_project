import kit from "./kit.module.css";

// 백엔드 API가 아직 없는 기능의 자리 표시. API가 생기면 이 컴포넌트로 가는 분기를 지운다.
export function ComingSoon({ feature }: { feature: string }) {
  return (
    <section className={kit.card} aria-label={feature}>
      <p className={kit.empty}>{feature} 기능은 준비 중이에요.</p>
    </section>
  );
}
