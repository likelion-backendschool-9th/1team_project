package com.dipsource.dto.coin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** 칩 충전 요청. packageId는 1~4 패키지 번호 */
public record ChipChargeRequest(
        @NotNull @Min(1) Integer packageId
) {}
