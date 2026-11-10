package com.dipsource.dto.settlement;

/**
 * 하루 수익. 거래가 없어도 0으로 한 칸을 채운다.
 *
 * @param date  yyyy-MM-dd
 * @param chips 수익 칩
 * @param gross 원화. 칩 x 칩 단가
 * @param count 수익 거래 건수
 */
public record MyDailyIncomeResponse(String date, int chips, int gross, int count) {
}
