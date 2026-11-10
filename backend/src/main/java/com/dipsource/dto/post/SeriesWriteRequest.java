package com.dipsource.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * 시리즈 만들기·수정 요청 (같은 모양이라 함께 쓴다).
 *
 * @param title       제목 (1~80자)
 * @param description 소개 (최대 500자). 없으면 빈 문자열
 * @param price       판매 가격(원). 0이면 무료 시리즈. 상한·하한 없음 (팀 결정)
 * @param status      "ongoing"(연재 중) / "completed"(완결). 만들 때는 비워도 되고 연재 중이 된다
 */
public record SeriesWriteRequest(
        @NotBlank @Size(max = 80) String title,
        @Size(max = 500) String description,
        @NotNull @PositiveOrZero Integer price,
        String status
) {
}
