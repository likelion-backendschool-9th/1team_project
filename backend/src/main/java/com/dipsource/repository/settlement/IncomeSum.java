package com.dipsource.repository.settlement;

/** 기간 안 회원별 수익 칩 합계 (정산 생성용 집계 결과) */
public record IncomeSum(Long userId, Long chips) {
}
