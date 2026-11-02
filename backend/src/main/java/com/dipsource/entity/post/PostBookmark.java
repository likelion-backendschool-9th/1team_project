package com.dipsource.entity.post;

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

/** 블로그 글 북마크. 테이블: post_bookmarks */
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "post_bookmarks",
        indexes = @Index(name = "idx_post_bookmarks_post", columnList = "post_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostBookmark {

    /** 회원 ID·글 ID 복합 키 */
    @EmbeddedId
    private PostUserId id;

    /** 대상 글 */
    @MapsId("postId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    /** 북마크한 시각 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public PostBookmark(Long userId, Post post) {
        this.id = new PostUserId(userId, post.getId());
        this.post = post;
    }
}
