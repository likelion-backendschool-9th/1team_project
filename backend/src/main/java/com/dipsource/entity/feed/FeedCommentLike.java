package com.dipsource.entity.feed;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
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

/** 피드 댓글 좋아요. 테이블: feed_comment_likes */
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "feed_comment_likes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedCommentLike {

    /** 회원 ID·댓글 ID 복합 키 */
    @EmbeddedId
    private FeedCommentLikeId id;

    /** 대상 댓글 */
    @MapsId("commentId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comment_id", nullable = false)
    private FeedComment comment;

    /** 좋아요한 시각 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public FeedCommentLike(Long userId, FeedComment comment) {
        this.id = new FeedCommentLikeId(userId, comment.getId());
        this.comment = comment;
    }
}
