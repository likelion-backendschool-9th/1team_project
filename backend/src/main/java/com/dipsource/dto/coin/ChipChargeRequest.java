package com.dipsource.dto.coin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 칩 충전 요청.
 *
 * @param packageId      패키지 번호 (1~4)
 * @param idempotencyKey 클라이언트가 생성한 멱등 키 (선택).
 *                       같은 키로 재요청하면 중복 처리 없이 성공으로 응답한다.
 *                       PG 연동 후에는 PG가 발급한 주문 번호로 교체된다.
 */
public record ChipChargeRequest(
        @NotNull @Min(1) Integer packageId,
        @Size(max = 100) String idempotencyKey
) {}
