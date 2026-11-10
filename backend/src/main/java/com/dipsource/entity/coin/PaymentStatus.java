package com.dipsource.entity.coin;

/** 결제 상태 */
public enum PaymentStatus {
    /** 결제 요청만 생성(PG 승인 전) */
    READY,
    /** 결제 완료 */
    PAID,
    /** 승인 전 취소·실패 */
    CANCELED,
    /** 결제 후 환불 */
    REFUNDED
}
