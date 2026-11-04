import { NextResponse, type NextRequest } from "next/server";

const ACCESS = "access_token";
const REFRESH = "refresh_token";
const API_ORIGIN = process.env.API_ORIGIN;

/**
 * Access Token이 만료돼 쿠키에서 사라졌고 Refresh Token은 남아 있으면,
 * 페이지를 그리기 전에 백엔드에서 새 토큰을 받아 온다.
 */
export async function proxy(request: NextRequest) {
  const hasAccess = request.cookies.has(ACCESS);
  const refreshToken = request.cookies.get(REFRESH)?.value;

  // 목 모드이거나, 갱신이 필요 없으면 그대로 통과
  if (!API_ORIGIN || hasAccess || !refreshToken) return NextResponse.next();

  const res = await fetch(`${API_ORIGIN}/api/v1/auth/refresh`, {
    method: "POST",
    headers: { cookie: `${REFRESH}=${refreshToken}` },
  });

  // 갱신 실패: 남은 refresh 쿠키를 지워 로그아웃 상태로 만든다
  if (!res.ok) {
    const response = NextResponse.next();
    response.cookies.delete(REFRESH);
    return response;
  }

  // 백엔드가 보낸 Set-Cookie 목록 ("access_token=...; Path=/; HttpOnly; ...")
  const setCookies = res.headers.getSetCookie();

  // ① 이번 요청: 서버 컴포넌트가 새 Access Token을 보도록 요청 쿠키를 바꿔 끼운다
  const cookieMap = new Map(request.cookies.getAll().map((c) => [c.name, c.value]));
  for (const header of setCookies) {
    const [pair] = header.split(";"); // "access_token=eyJ..."
    const i = pair.indexOf("=");
    cookieMap.set(pair.slice(0, i), pair.slice(i + 1));
  }
  const requestHeaders = new Headers(request.headers);
  requestHeaders.set(
    "cookie",
    [...cookieMap].map(([name, value]) => `${name}=${value}`).join("; "),
  );
  const response = NextResponse.next({ request: { headers: requestHeaders } });

  // ② 다음 요청부터: 브라우저에 새 쿠키를 저장시킨다 (Secure·HttpOnly 속성 그대로)
  for (const header of setCookies) response.headers.append("set-cookie", header);
  return response;
}

export const config = {
  // API, 정적 파일, 이미지는 건너뛴다
  matcher: ["/((?!api|_next/static|_next/image|favicon.ico|icon.svg).*)"],
};