package com.dipsource.repository.settlement;

import java.time.LocalDate;

/** 하루 수익 칩 합과 거래 건수. 수익이 있는 날만 조회된다. */
public record DailyIncome(LocalDate date, Long chips, Long count) {
}
