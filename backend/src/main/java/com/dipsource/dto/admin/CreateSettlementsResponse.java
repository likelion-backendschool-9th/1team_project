package com.dipsource.dto.admin;

/**
 * 정산 만들기 결과.
 *
 * @param period  정산한 달 "YYYY-MM"
 * @param created 새로 만든 정산 수
 * @param skipped 이미 정산이 있어 건너뛴 작가 수
 */
public record CreateSettlementsResponse(String period, int created, int skipped) {
}
