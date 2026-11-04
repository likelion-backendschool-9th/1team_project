import { redirect } from "next/navigation";
import { getMe } from "../_lib/api";

/**
 * 온보딩을 마치지 않은 회원이 회원 화면에 들어오면 온보딩으로 보낸다.
 * 비로그인(me가 null)이거나 내 정보를 못 불러오면 그대로 둔다.
 */
export async function OnboardingGuard() {
  let me;
  try {
    me = await getMe();
  } catch {
    return null; // 백엔드 오류로 전체 화면이 깨지지 않게 넘어간다
  }

  if (me?.onboardingIncomplete) redirect("/onboarding");
  return null;
}