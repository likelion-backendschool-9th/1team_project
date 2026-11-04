package com.dipsource.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** TEMP-DEV-LOGIN: 로컬 시드 계정 로그인. 배포에서는 app.dev-login-enabled=false. */
public record DevLoginRequest(
        @NotBlank @Email String email
) {
}
