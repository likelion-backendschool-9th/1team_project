package com.dipsource.global.auth;

/** JWT를 담는 httpOnly 쿠키 이름. */
public final class AuthCookieNames {

    /** Access Token 쿠키 이름 */
    public static final String ACCESS = "access_token";

    /** Refresh Token 쿠키 이름 */
    public static final String REFRESH = "refresh_token";

    private AuthCookieNames() {
    }
}
