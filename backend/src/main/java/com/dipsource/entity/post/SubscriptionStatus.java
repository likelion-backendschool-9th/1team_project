package com.dipsource.entity.post;

/** 블로그 구독 상태 */
public enum SubscriptionStatus {
    /** 구독 중 */
    ACTIVE,
    /** 구독자가 해지(만료일까지는 이용 가능) */
    CANCELED,
    /** 만료 */
    EXPIRED
}
