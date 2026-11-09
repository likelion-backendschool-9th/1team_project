package com.dipsource.repository.post;

import com.dipsource.entity.post.BlogSubscription;
import com.dipsource.entity.post.SubscriptionStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** blog_subscriptions 테이블 접근 */
public interface BlogSubscriptionRepository extends JpaRepository<BlogSubscription, Long> {

    /** 지금 구독 중인지 (상태가 ACTIVE이고 아직 만료 전) */
    boolean existsBySubscriberIdAndBlogOwnerIdAndStatusAndExpiresAtAfter(
            Long subscriberId, Long blogOwnerId, SubscriptionStatus status, LocalDateTime now);

    /**
     * 유료 글을 읽을 수 있는 구독인지. 해지(CANCELED)해도 만료일까지는 읽을 수 있다.
     */
    @Query("""
            SELECT COUNT(s) > 0 FROM BlogSubscription s
            WHERE s.subscriberId = :subscriberId AND s.blogOwnerId = :blogOwnerId
              AND s.status IN (com.dipsource.entity.post.SubscriptionStatus.ACTIVE,
                               com.dipsource.entity.post.SubscriptionStatus.CANCELED)
              AND s.expiresAt > :now
            """)
    boolean existsReadable(
            @Param("subscriberId") Long subscriberId,
            @Param("blogOwnerId") Long blogOwnerId,
            @Param("now") LocalDateTime now);

    /** 이 블로그에 대해 아직 만료 전인 내 구독 (ACTIVE·CANCELED). 가장 늦게 끝나는 것 하나 */
    @Query("""
            SELECT s FROM BlogSubscription s
            WHERE s.subscriberId = :subscriberId AND s.blogOwnerId = :blogOwnerId
              AND s.status IN (com.dipsource.entity.post.SubscriptionStatus.ACTIVE,
                               com.dipsource.entity.post.SubscriptionStatus.CANCELED)
              AND s.expiresAt > :now
            ORDER BY s.expiresAt DESC
            LIMIT 1
            """)
    Optional<BlogSubscription> findCurrent(
            @Param("subscriberId") Long subscriberId,
            @Param("blogOwnerId") Long blogOwnerId,
            @Param("now") LocalDateTime now);

    /** 내 구독 목록. 아직 만료 전인 것만, 최근 구독 순 */
    @Query("""
            SELECT s FROM BlogSubscription s
            WHERE s.subscriberId = :subscriberId
              AND s.status IN (com.dipsource.entity.post.SubscriptionStatus.ACTIVE,
                               com.dipsource.entity.post.SubscriptionStatus.CANCELED)
              AND s.expiresAt > :now
            ORDER BY s.startedAt DESC, s.id DESC
            """)
    List<BlogSubscription> findCurrentBySubscriber(
            @Param("subscriberId") Long subscriberId,
            @Param("now") LocalDateTime now);

    /** 지금 구독 중인(ACTIVE, 만료 전) 사람 수 */
    long countByBlogOwnerIdAndStatusAndExpiresAtAfter(Long blogOwnerId, SubscriptionStatus status, LocalDateTime now);
}
