package com.dipsource.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
    @NotBlank(message = "닉네임을 입력해 주세요.")
    @Size(min = 2, max = 30, message = "닉네임은 2 ~ 30자로 입력해 주세요.")
    String nickname,

    @Size(max = 200, message = "자기소개는 최대 200자 입니다.")
    String bio,

    String techStacks,

    @Size(max = 200, message = "GitHub 주소가 너무 깁니다.")
    String githubUrl
) {
}
