package com.dipsource.dto.minihome;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

/** 내 미니홈피 수정 요청. stackTags는 쉼표로 구분한 문자열이다. featuredPostId는 받지 않는다. */
public record UpdateMiniHomeRequest(
        @NotBlank(message = "한 줄 소개를 입력해 주세요.")
        @Size(max = 30, message = "한 줄 소개는 30자 이하로 입력해 주세요.")
        String headline,

        @Size(max = 80, message = "오늘의 한마디는 80자 이하로 입력해 주세요.")
        String quote,

        @Size(max = 50, message = "스킨 이름이 너무 길어요.")
        String skin,

        @Size(max = 100, message = "개인 도메인은 100자 이하로 입력해 주세요.")
        String domain,

        @Size(max = 50, message = "배경음악 값이 너무 길어요.")
        String bgm,

        /** "React, TypeScript"처럼 쉼표로 구분한다. 비어 있으면 태그를 지운다. */
        String stackTags,

        /** 비어 있어도 된다. 데모가 아직 없는 회원도 미니홈피를 저장할 수 있게 한다. */
        @Size(max = 6, message = "공개할 데모는 6개까지 고를 수 있어요.")
        List<Long> showcaseDemoIds,

        /** 비어 있으면 목록 순서를 그대로 둔다. 프론트는 없을 때 빈 문자열을 보낸다. */
        String featuredDemoId
) {
}
