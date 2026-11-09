package com.dipsource.dto.donation;

import jakarta.validation.constraints.Min;

/** 후원 요청 DTO */
public record DonationRequest(
        @Min(value = 1, message = "후원 칩 수는 1개 이상이어야 해요") int chips
) {}
