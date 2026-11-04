package com.dipsource.dto.auth;

/** TEMP-DEV-LOGIN: 로컬 시드 계정 로그인 응답. 프론트가 쿠키에 심는다. */
public record DevLoginResponse(
        String accessToken,
        String refreshToken,
        Long userId,
        String nickname,
        String email
) {
}
