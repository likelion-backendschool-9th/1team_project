package com.dipsource.entity.coin;

/** 칩 거래가 가리키는 원본 기록의 종류 */
public enum ChipRefType {
    /** post_purchases */
    POST_PURCHASE,
    /** donations */
    DONATION,
    /** payments */
    PAYMENT,
    /** settlements */
    SETTLEMENT
}
