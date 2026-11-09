package com.dipsource.global.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtProviderTest {

    private final JwtProvider jwtProvider = new JwtProvider(new JwtProperties(
            "test-secret-test-secret-test-secret-test-secret-0000",
            1800,
            1209600));

    @Test
    @DisplayName("같은 jti·iat·exp로 다시 서명하면 Refresh 문자열이 같다")
    void recreateRefreshTokenMatchesOriginal() {
        JwtProvider.IssuedRefreshToken issued =
                jwtProvider.createRefreshToken(1L, UUID.randomUUID());

        String again = jwtProvider.recreateRefreshToken(
                1L, issued.jti(), issued.issuedAt(), issued.expiresAt());

        assertThat(again).isEqualTo(issued.token());
        assertThat(jwtProvider.isRefreshToken(again)).isTrue();
        assertThat(jwtProvider.getUserId(again)).isEqualTo(1L);
    }
}
