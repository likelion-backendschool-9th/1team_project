package com.dipsource.dto.post;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * 내 블로그 구독료 변경 요청. 상한·하한은 두지 않는다 (팀 결정).
 *
 * @param price 월 구독료(원). 0이면 구독을 받지 않는다
 */
public record ChangeSubscriptionPriceRequest(@NotNull @PositiveOrZero Integer price) {
}
