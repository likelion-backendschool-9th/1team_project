package com.dipsource.dto.admin;

import com.dipsource.entity.post.BlogSubscription;
import com.dipsource.entity.post.SubscriptionStatus;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 관리자 구독 목록 한 건 (프론트 AdminSubscription).
 *
 * @param plan   지금은 블로그 구독만 있어서 항상 "blog" (딥소스 플러스는 아직 없다)
 * @param price  구독 시점 가격(원)
 * @param status 지금 구독 중이면 "active", 해지했거나 만료됐으면 "canceled"
 */
public record AdminSubscriptionResponse(
        Long id,
        String plan,
        String subscriber,
        String author,
        int price,
        String startedAt,
        String expiresAt,
        String status
) {
    public static AdminSubscriptionResponse of(BlogSubscription subscription, String subscriber, String author,
                                               LocalDateTime now) {
        boolean active = subscription.getStatus() == SubscriptionStatus.ACTIVE && subscription.getExpiresAt().isAfter(now);
        return new AdminSubscriptionResponse(
                subscription.getId(),
                "blog",
                subscriber,
                author,
                subscription.getPaidPrice(),
                subscription.getStartedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                subscription.getExpiresAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                active ? "active" : "canceled"
        );
    }
}
