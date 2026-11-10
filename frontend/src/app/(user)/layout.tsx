import { Suspense, type ReactNode } from "react";
import { DmWidget } from "./_components/DmWidget";
import { OnboardingGuard } from "./_components/OnboardingGuard";
import { RealtimeProvider } from "./_components/RealtimeProvider";
import { RealtimeSession } from "./_components/RealtimeSession";

// 피드·블로그·미니홈피 등 회원 화면 전체에 실시간 연결, DM 버튼과 DM 창을 붙이고,
// 온보딩을 마치지 않은 회원은 온보딩으로 보낸다.
export default function UserGroupLayout({ children }: { children: ReactNode }) {
  return (
    <RealtimeProvider>
      <DmWidget>
        <Suspense fallback={null}>
          <OnboardingGuard />
          <RealtimeSession />
        </Suspense>
        {children}
      </DmWidget>
    </RealtimeProvider>
  );
}
