package com.dipsource.entity.coin;

/** 칩 거래 종류. 괄호는 잔액 증감 방향 */
public enum ChipTransactionType {
    /** 칩 충전(+) */
    CHARGE,
    /** 유료 글 구매(-) */
    BLOG_PURCHASE,
    /** 내 유료 글 판매 수익(+) */
    BLOG_INCOME,
    /** 후원 보냄(-) */
    DONATION,
    /** 후원 받음(+) */
    DONATION_INCOME,
    /** 칩을 현금으로 환전(-) */
    REFUND,
    /** 정산으로 차감(-) */
    SETTLEMENT,
    /** 관리자 조정(±) */
    ADMIN_ADJUST
}
