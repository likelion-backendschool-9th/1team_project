package com.dipsource.dto.settlement;

/** 정산 생성 결과. created는 새로 만든 건수, skipped는 이미 있어서 건너뛴 건수 */
public record SettlementGenerateResponse(String period, int created, int skipped) {
}
