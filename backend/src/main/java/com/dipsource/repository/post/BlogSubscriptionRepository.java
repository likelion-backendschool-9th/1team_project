package com.dipsource.repository.post;

import com.dipsource.entity.post.BlogSubscription;
import com.dipsource.entity.post.SubscriptionStatus;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

/** blog_subscriptions 테이블 접근 */
public interface BlogSubscriptionRepository extends JpaRepository<BlogSubscription, Long> {

    /** 지금 구독 중인지 (상태가 ACTIVE이고 아직 만료 전) */
    boolean existsBySubscriberIdAndBlogOwnerIdAndStatusAndExpiresAtAfter(
            Long subscriberId, Long blogOwnerId, SubscriptionStatus status, LocalDateTime now);
}
