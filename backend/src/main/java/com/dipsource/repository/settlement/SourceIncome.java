package com.dipsource.repository.settlement;

import com.dipsource.entity.coin.ChipRefType;

/** 출처(ChipRefType)별 수익 칩 합과 거래 건수. */
public record SourceIncome(ChipRefType refType, Long chips, Long count) {
}
