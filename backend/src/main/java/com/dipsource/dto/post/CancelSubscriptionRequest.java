package com.dipsource.dto.post;

/**
 * 구독 해지 요청. 블로그 구독이면 subscriptionId, 딥소스 플러스면 plan="plus" (플러스는 아직 없다).
 *
 * @param subscriptionId 해지할 블로그 구독(blog_subscriptions.id)
 * @param plan           "plus"면 딥소스 플러스
 */
public record CancelSubscriptionRequest(Long subscriptionId, String plan) {
}
