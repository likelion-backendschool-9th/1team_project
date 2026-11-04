package com.dipsource.dto.minihome;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

/** 내 미니홈피 수정 요청. stackTags는 회원 기술 스택이라 받지 않는다. */
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

        @NotEmpty(message = "공개할 데모를 1개 이상 골라 주세요.")
        @Size(min = 1, max = 3, message = "공개할 데모는 1~3개만 고를 수 있어요.")
        List<Long> showcaseDemoIds,

        /** 비어 있으면 목록 순서를 그대로 둔다. 프론트는 없을 때 빈 문자열을 보낸다. */
        String featuredDemoId
) {
}
