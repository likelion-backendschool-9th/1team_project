import { getMe } from "../_lib/api";
import { getSettings } from "../(feed)/my/_lib/api";
import { RealtimeLogin } from "./RealtimeProvider";

/**
 * 로그인했으면 실시간 연결을 켠다. 비로그인이거나 내 정보를 못 불러오면 연결하지 않는다.
 * DM 알림 설정을 못 불러오면 켜진 것으로 본다(기본값).
 */
export async function RealtimeSession() {
  let me;
  try {
    me = await getMe();
  } catch {
    return null;
  }
  if (!me) return null;

  const notifyDm = await getSettings()
    .then((settings) => settings.notify.dm)
    .catch(() => true);
  return <RealtimeLogin me={{ id: me.id, notifyDm }} />;
}
