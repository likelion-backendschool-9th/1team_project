package com.dipsource.global.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 관련 설정값.
 * application.yml의 app.jwt.* 값을 담는다.
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        /** JWT 서명에 쓰는 비밀키 */
        String secret,
        /** Access Token 유효 시간(초) */
        long accessTokenValidity,
        /** Refresh Token 유효 시간(초) */
        long refreshTokenValidity
) {
}
