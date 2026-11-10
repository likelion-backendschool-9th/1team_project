package com.dipsource.entity.post;

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

/** 블로그 댓글·답글. parent가 있으면 답글이다. 테이블: post_comments */
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "post_comments",
        indexes = @Index(name = "idx_post_comments_post_created", columnList = "post_id, created_at"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostComment {

    /** 댓글 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 댓글이 달린 글 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    /** 작성자 ID(users.id) */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 부모 댓글. 답글이 아니면 null */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private PostComment parent;

    /** 내용 */
    @Column(nullable = false, length = 1000)
    private String content;

    /** 좋아요 수 */
    @Column(name = "like_count", nullable = false)
    private int likeCount = 0;

    /** 작성 시각 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 수정 시각. 수정한 적 없으면 null */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** 삭제 시각. 삭제하지 않았으면 null */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    private PostComment(Post post, Long userId, PostComment parent, String content) {
        this.post = post;
        this.userId = userId;
        this.parent = parent;
        this.content = content;
    }

    public void update(String content) {
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
