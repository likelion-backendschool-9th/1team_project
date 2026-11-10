package com.dipsource.entity.coin;

/** 결제 종류 */
public enum PaymentType {
    /** 칩 충전 */
    CHIP_CHARGE,
    /** 블로그 구독 (블로그 구독을 없애기 전 기록) */
    BLOG_SUBSCRIPTION,
    /** 시리즈 구매 */
    SERIES_PURCHASE,
    /** 딥소스 플러스 자동 갱신. PG 연동 전에는 가상 결제로 바로 PAID가 된다 */
    PLUS
}
