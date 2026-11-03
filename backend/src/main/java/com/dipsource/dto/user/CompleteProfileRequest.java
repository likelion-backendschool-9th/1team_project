package com.dipsource.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

/** 온보딩 프로필 저장 요청. */
public record CompleteProfileRequest(
        @NotBlank(message = "닉네임을 입력해 주세요.")
        @Size(min = 2, max = 30, message = "닉네임은 2~30자로 입력해 주세요.")
        String nickname,

        @Size(max = 200, message = "자기소개는 200자 이하로 입력해 주세요.")
        String bio,

        @Size(max = 50, message = "GitHub 사용자명이 너무 길어요.")
        String githubUsername,

        @NotEmpty(message = "기술 스택을 2개 이상 골라 주세요.")
        @Size(min = 2, message = "기술 스택을 2개 이상 골라 주세요.")
        List<String> techStacks,

        @NotEmpty(message = "관심 태그를 2개 이상 골라 주세요.")
        @Size(min = 2, message = "관심 태그를 2개 이상 골라 주세요.")
        List<String> interestTags
) {
}
