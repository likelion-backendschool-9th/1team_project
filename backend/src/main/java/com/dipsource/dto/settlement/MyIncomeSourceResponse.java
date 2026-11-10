package com.dipsource.dto.settlement;

import com.dipsource.entity.coin.ChipRefType;

/**
 * 출처별 수익 합계.
 *
 * @param refType ChipRefType. 출처를 모르면 null
 * @param label   화면 라벨
 * @param gross   원화. 칩 x 칩 단가
 */
public record MyIncomeSourceResponse(
        ChipRefType refType,
        String label,
        int chips,
        int gross,
        int count
) {
}
