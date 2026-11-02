package com.dipsource.entity.post;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 블로그 구독(월 결제). 구독자·작가는 user 기능이라 id만 가진다. 테이블: blog_subscriptions */
@Getter
@Entity
@Table(name = "blog_subscriptions", indexes = {
        @Index(name = "idx_blog_subscriptions_subscriber", columnList = "subscriber_id"),
        @Index(name = "idx_blog_subscriptions_owner", columnList = "blog_owner_id")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlogSubscription {

    /** 구독 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 구독한 회원 ID(users.id) */
    @Column(name = "subscriber_id", nullable = false)
    private Long subscriberId;

    /** 구독받는 블로그 주인 ID(users.id) */
    @Column(name = "blog_owner_id", nullable = false)
    private Long blogOwnerId;

    /** 결제한 금액(원). 구독 이후 가격이 바뀌어도 그대로 남긴다 */
    @Column(name = "paid_price", nullable = false)
    private int paidPrice;

    /** 상태 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    /** 구독 시작 시각 */
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    /** 구독 만료 시각 */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Builder
    private BlogSubscription(Long subscriberId, Long blogOwnerId, int paidPrice,
                             LocalDateTime startedAt, LocalDateTime expiresAt) {
        this.subscriberId = subscriberId;
        this.blogOwnerId = blogOwnerId;
        this.paidPrice = paidPrice;
        this.startedAt = startedAt;
        this.expiresAt = expiresAt;
    }

    public void cancel() {
        this.status = SubscriptionStatus.CANCELED;
    }

    public void expire() {
        this.status = SubscriptionStatus.EXPIRED;
    }

    /** 한 달 더 연장한다. */
    public void renew(LocalDateTime newExpiresAt) {
        this.status = SubscriptionStatus.ACTIVE;
        this.expiresAt = newExpiresAt;
    }
}
