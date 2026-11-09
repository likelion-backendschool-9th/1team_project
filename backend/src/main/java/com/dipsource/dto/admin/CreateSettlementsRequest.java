package com.dipsource.dto.admin;

/**
 * 정산 만들기 요청.
 *
 * @param month 정산할 달 "YYYY-MM". 비우면 지난달. 아직 끝나지 않은 달은 만들 수 없다
 */
public record CreateSettlementsRequest(String month) {
}
