package com.dipsource.global.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

/** Access / Refresh JWT를 만들고 검증하는 컴포넌트. */
@Component
public class JwtProvider {

    private final JwtProperties properties;
    private final SecretKey secretKey;

    public JwtProvider(JwtProperties properties) {
        this.properties = properties;
        // HMAC 서명용 키를 비밀 문자열에서 만든다.
        this.secretKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    /** API 요청 인증에 쓰는 Access Token을 발급한다. */
    public String createAccessToken(Long userId, String role) {
        return createToken(userId, role, properties.accessTokenValidity());
    }

    /** Access Token 재발급에 쓰는 Refresh Token을 발급한다. */
    public String createRefreshToken(Long userId) {
        return createToken(userId, null, properties.refreshTokenValidity());
    }

    /** 토큰이 유효한지 확인한다. */
    public boolean validate(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** 토큰에서 회원 ID를 꺼낸다. */
    public Long getUserId(String token) {
        return Long.valueOf(parseClaims(token).getSubject());
    }

    /** 토큰에서 권한(role)을 꺼낸다. 없으면 null. */
    public String getRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    private String createToken(Long userId, String role, long validitySeconds) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validitySeconds * 1000);

        var builder = Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiry);

        // Access Token에만 role claim을 넣는다.
        if (role != null) {
            builder.claim("role", role);
        }

        return builder.signWith(secretKey).compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
