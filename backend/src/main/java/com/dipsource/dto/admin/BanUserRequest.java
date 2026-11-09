package com.dipsource.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 관리자 회원 제재 요청. PUT /admin/users/{userId}/ban */
public record BanUserRequest(
        @NotBlank String period,
        @NotBlank @Size(min = 5, max = 500) String reason
) {}