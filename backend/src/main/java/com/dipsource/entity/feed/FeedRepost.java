package com.dipsource.entity.feed;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** 피드 리포스트. 테이블: feed_reposts */
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "feed_reposts",
        indexes = @Index(name = "idx_feed_reposts_feed", columnList = "feed_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedRepost {

    /** 회원 ID·피드 ID 복합 키 */
    @EmbeddedId
    private FeedUserId id;

    /** 대상 피드 */
    @MapsId("feedId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;

    /** 리포스트한 시각 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public FeedRepost(Long userId, Feed feed) {
        this.id = new FeedUserId(userId, feed.getId());
        this.feed = feed;
    }
}
