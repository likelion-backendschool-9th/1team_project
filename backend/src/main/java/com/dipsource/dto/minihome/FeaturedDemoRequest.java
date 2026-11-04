package com.dipsource.dto.minihome;

import jakarta.validation.constraints.NotNull;

/** 대표 데모 변경 요청. */
public record FeaturedDemoRequest(
        @NotNull(message = "대표 데모를 골라 주세요.")
        Long demoId
) {
}
