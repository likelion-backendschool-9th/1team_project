package com.dipsource.global.auth;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/** JWT를 httpOnly 쿠키로 심거나 지우는 도우미. */
@Component
public class AuthCookieWriter {

    private final JwtProperties jwtProperties;

    public AuthCookieWriter(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    /** 로그인 성공 시 Access / Refresh 쿠키를 응답에 넣는다. */
    public void writeTokens(HttpServletResponse response, String accessToken, String refreshToken) {
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie(
                AuthCookieNames.ACCESS, accessToken, jwtProperties.accessTokenValidity()).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie(
                AuthCookieNames.REFRESH, refreshToken, jwtProperties.refreshTokenValidity()).toString());
    }

    /** 로그아웃 시 인증 쿠키를 바로 만료시킨다. */
    public void clear(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie(AuthCookieNames.ACCESS, "", 0).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie(AuthCookieNames.REFRESH, "", 0).toString());
    }

    private ResponseCookie buildCookie(String name, String value, long maxAgeSeconds) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)   // JS에서 읽지 못하게 한다
                .secure(false)    // 로컬 HTTP용. 배포 시 true로 바꾼다
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAgeSeconds)
                .build();
    }
}
