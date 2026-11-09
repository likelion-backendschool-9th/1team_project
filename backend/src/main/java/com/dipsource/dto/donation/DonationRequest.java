package com.dipsource.dto.donation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/** 후원 요청 DTO */
public record DonationRequest(
        @Min(value = 1, message = "후원 칩 수는 1개 이상이어야 해요")
        @Max(value = 10000, message = "1회 후원은 10,000칩 이하여야 해요")
        int chips
) {}
