package com.dipsource.entity.post;

import com.dipsource.global.common.BaseEntity;
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

/**
 * 블로그 글. 테이블: posts
 * 조회·좋아요 수는 동시성 때문에 엔티티에서 더하지 않고 리포지토리의 UPDATE 쿼리로 갱신한다.
 */
@Getter
@Entity
@Table(name = "posts", indexes = {
        @Index(name = "idx_posts_user_created", columnList = "user_id, created_at"),
        @Index(name = "idx_posts_created", columnList = "created_at")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {

    /** 글 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 작성자 ID(users.id) */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 연결한 데모 ID(demos.id). 제작기 글이 아니면 null */
    @Column(name = "demo_id")
    private Long demoId;

    /** 제목 */
    @Column(nullable = false, length = 200)
    private String title;

    /** 본문(마크다운) */
    @Column(nullable = false, length = 20000)
    private String content;

    /** 썸네일 이미지 URL */
    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    /** 유료 글 여부. 유료 글은 블로그 구독자에게 무료이고, 구독하지 않은 회원만 단건 구매할 수 있다 */
    @Column(name = "is_paid", nullable = false)
    private boolean paid = false;

    /** 유료 글 단일 결제 가격(칩) */
    @Column(name = "price_chips", nullable = false)
    private int priceChips = 0;

    /** 상태 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostStatus status = PostStatus.ACTIVE;

    /** 조회 수 */
    @Column(name = "view_count", nullable = false)
    private int viewCount = 0;

    /** 좋아요 수 */
    @Column(name = "like_count", nullable = false)
    private int likeCount = 0;

    /** 댓글 수 */
    @Column(name = "comment_count", nullable = false)
    private int commentCount = 0;

    /** 삭제 시각. 삭제하지 않았으면 null */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    private Post(Long userId, Long demoId, String title, String content, String thumbnailUrl,
                 boolean paid, int priceChips) {
        this.userId = userId;
        this.demoId = demoId;
        this.title = title;
        this.content = content;
        this.thumbnailUrl = thumbnailUrl;
        this.paid = paid;
        this.priceChips = paid ? priceChips : 0;
    }

    public void update(String title, String content, String thumbnailUrl, Long demoId,
                       boolean paid, int priceChips) {
        this.title = title;
        this.content = content;
        this.thumbnailUrl = thumbnailUrl;
        this.demoId = demoId;
        this.paid = paid;
        this.priceChips = paid ? priceChips : 0;
    }

    public void hide() {
        this.status = PostStatus.HIDDEN;
    }

    public void delete() {
        this.status = PostStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }
}
