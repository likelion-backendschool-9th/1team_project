package com.dipsource.dto.post;

import java.time.LocalDateTime;

/**
 * 내가 산 시리즈 한 건.
 *
 * @param paidPrice   결제한 금액(원). 산 뒤에 가격이 바뀌어도 그대로
 * @param purchasedAt 산 시각
 */
public record MySeriesPurchaseResponse(
        SeriesSummaryResponse series,
        int paidPrice,
        LocalDateTime purchasedAt
) {
}
