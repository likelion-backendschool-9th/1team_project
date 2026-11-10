package com.dipsource.entity.feed;

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
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * 피드(짧은 글). 테이블: feeds
 * 좋아요·댓글·리포스트 수는 동시성 때문에 엔티티에서 더하지 않고 리포지토리의 UPDATE 쿼리로 갱신한다.
 */
@Getter
@Entity
@Table(name = "feeds", indexes = {
        @Index(name = "idx_feeds_user_created", columnList = "user_id, created_at"),
        @Index(name = "idx_feeds_created", columnList = "created_at")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Feed extends BaseEntity {

    /** 피드 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 작성자 ID(users.id) */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 내용 */
    @Column(nullable = false, length = 500)
    private String content;

    /** 첨부한 데모 ID(demos.id). 첨부하지 않았으면 null */
    @Column(name = "demo_id")
    private Long demoId;

    /** 상태 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FeedStatus status = FeedStatus.ACTIVE;

    /** 좋아요 수 */
    @Column(name = "like_count", nullable = false)
    private int likeCount = 0;

    /** 댓글 수 */
    @Column(name = "comment_count", nullable = false)
    private int commentCount = 0;

    /** 리포스트 수 */
    @Column(name = "repost_count", nullable = false)
    private int repostCount = 0;

    /** 첨부 이미지 URL 목록. 이미지가 없으면 null */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "image_urls", columnDefinition = "jsonb")
    private List<String> imageUrls;

    /** 첨부 링크 URL. 링크가 없으면 null */
    @Column(name = "link_url", length = 500)
    private String linkUrl;

    /** 투표 선택지. 투표가 없는 피드면 null. 예: ["Spring", "Node"] */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "poll_options", columnDefinition = "jsonb")
    private List<String> pollOptions;

    /** 삭제 시각. 삭제하지 않았으면 null */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    private Feed(Long userId, String content, Long demoId,
                 List<String> imageUrls, String linkUrl, List<String> pollOptions) {
        this.userId = userId;
        this.content = content;
        this.demoId = demoId;
        this.imageUrls = imageUrls == null ? null : new ArrayList<>(imageUrls);
        this.linkUrl = linkUrl;
        this.pollOptions = pollOptions == null ? null : new ArrayList<>(pollOptions);
    }

    public void update(String content) {
        this.content = content;
    }

    public void hide() {
        this.status = FeedStatus.HIDDEN;
    }

    public void delete() {
        this.status = FeedStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }
}
