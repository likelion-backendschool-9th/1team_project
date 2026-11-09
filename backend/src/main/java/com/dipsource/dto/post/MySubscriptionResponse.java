package com.dipsource.dto.post;

import com.dipsource.dto.user.UserSummaryResponse;
import com.dipsource.entity.post.BlogSubscription;
import com.dipsource.entity.post.SubscriptionStatus;
import java.time.LocalDateTime;

/**
 * 내 블로그 구독 한 건 (프론트 Subscription).
 *
 * @param pricePerMonth 구독 시점 가격(원)
 * @param nextBillingAt 이번 구독이 끝나는 시각. 자동 갱신은 PG 연동 후라, 지금은 이 날까지 읽을 수 있다
 * @param status        "active" / "canceled" (해지해도 만료일까지는 읽을 수 있다)
 */
public record MySubscriptionResponse(
        Long id,
        UserSummaryResponse author,
        String blogTitle,
        int pricePerMonth,
        LocalDateTime nextBillingAt,
        String status
) {
    public static MySubscriptionResponse of(BlogSubscription subscription, UserSummaryResponse author) {
        return new MySubscriptionResponse(
                subscription.getId(),
                author,
                author.nickname() + "의 개발 블로그",
                subscription.getPaidPrice(),
                subscription.getExpiresAt(),
                subscription.getStatus() == SubscriptionStatus.ACTIVE ? "active" : "canceled"
        );
    }
}
