package com.dipsource.entity.feed;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** 피드 투표 참여. 테이블: feed_votes */
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "feed_votes",
        indexes = @Index(name = "idx_feed_votes_feed_user", columnList = "feed_id, user_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedVote {

    /** 투표 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 투표한 피드 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;

    /** 투표한 회원 ID(users.id) */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 고른 선택지 번호. feeds.poll_options의 0부터 시작하는 순서 */
    @Column(name = "option_index", nullable = false, columnDefinition = "smallint")
    private short optionIndex;

    /** 투표한 시각 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private FeedVote(Feed feed, Long userId, short optionIndex) {
        this.feed = feed;
        this.userId = userId;
        this.optionIndex = optionIndex;
    }
}
