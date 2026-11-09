package com.dipsource.dto.post;

/**
 * 블로그 주인 화면에 보여 줄 구독 정보.
 *
 * @param price           월 구독료(원). 0이면 구독을 받지 않는다
 * @param subscribed      내가 지금 구독 중인지 (해지했으면 false). 비로그인이면 false
 * @param subscriberCount 지금 구독 중인 사람 수
 */
public record BlogSubscriptionInfoResponse(int price, boolean subscribed, long subscriberCount) {
}
