package com.dipsource.global.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

/** Access / Refresh JWT를 만들고 검증하는 컴포넌트. */
@Component
public class JwtProvider {

    private static final String TYPE_CLAIM = "type";
    private static final String TYPE_ACCESS = "ACCESS";
    private static final String TYPE_REFRESH = "REFRESH";

    private final JwtProperties properties;
    private final SecretKey secretKey;

    public JwtProvider(JwtProperties properties) {
        this.properties = properties;
        // HMAC 서명용 키를 비밀 문자열에서 만든다.
        this.secretKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    /** API 요청 인증에 쓰는 Access Token을 발급한다. */
    public String createAccessToken(Long userId, String role) {
        return createToken(userId, role, properties.accessTokenValidity(), TYPE_ACCESS);
    }

    public boolean isAccessToken(String token) {
        return hasType(token, TYPE_ACCESS);
    }

    public boolean isRefreshToken(String token) {
        return hasType(token, TYPE_REFRESH);
    }

    private boolean hasType(String token, String type) {
        try {
            return type.equals(parseClaims(token).get(TYPE_CLAIM, String.class));
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

    private String createToken(Long userId, String role, long validitySeconds, String type) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validitySeconds * 1000);

        var builder = Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(TYPE_CLAIM, type)
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

    public record IssuedRefreshToken(
        String token,
        String jti,
        UUID familyId,
        Instant issuedAt,
        Instant expiresAt
    ) {}

    
    /** 로그인·갱신 때 호출. familyId는 로그인이면 새 UUID, 갱신이면 기존 값. */
    public IssuedRefreshToken createRefreshToken(Long userId, UUID familyId) {
        String jti = UUID.randomUUID().toString();
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(properties.refreshTokenValidity());
        String token = buildRefreshToken(userId, jti, issuedAt, expiresAt);
        return new IssuedRefreshToken(token, jti, familyId, issuedAt, expiresAt);
    }

    public String recreateRefreshToken(Long userId, String jti, Instant issuedAt, Instant expiresAt) {
        return buildRefreshToken(userId, jti, issuedAt, expiresAt);
    }

    private String buildRefreshToken(Long userId, String jti, Instant issuedAt, Instant expiresAt) {
        return Jwts.builder()
                .id(jti)
                .subject(String.valueOf(userId))
                .claim(TYPE_CLAIM, TYPE_REFRESH)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }
}
