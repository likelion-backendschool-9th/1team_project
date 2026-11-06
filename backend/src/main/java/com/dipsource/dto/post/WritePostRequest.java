package com.dipsource.dto.post;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 글 작성·수정 요청 (같은 모양이라 함께 쓴다).
 *
 * @param title    제목 (1~80자)
 * @param category 분류 코드 (frontend, backend, ai, mobile, career, game, uxui)
 * @param tags     태그 이름 (최대 5개, 각 20자)
 * @param content  본문 마크다운 (20~20000자)
 * @param demoId   연결할 데모 ID. 없으면 null
 * @param price    유료 글 칩 가격. 0이면 무료, 1~100
 */
public record WritePostRequest(
        @NotBlank @Size(max = 80) String title,
        @NotBlank String category,
        @Size(max = 5) List<@NotBlank @Size(max = 20) String> tags,
        @NotBlank @Size(min = 20, max = 20000) String content,
        Long demoId,
        @NotNull @Min(0) @Max(100) Integer price
) {
}
