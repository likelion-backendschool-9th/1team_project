package com.dipsource.dto.admin;

/**
 * 한 달 매출(원). 결제 완료된 금액만이고 환불은 빠진다.
 *
 * @param month        "YYYY-MM"
 * @param subscription 블로그 구독료
 * @param plus         딥소스 플러스 (아직 없어서 0)
 * @param chips        칩 충전
 */
public record AdminRevenueResponse(String month, long subscription, long plus, long chips) {
}
