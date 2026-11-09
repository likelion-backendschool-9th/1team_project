package com.dipsource.entity.demo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

/** 데모 피드백(댓글)·답글. parent가 있으면 답글이다. 테이블: demo_feedbacks */
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "demo_feedbacks",
        indexes = @Index(name = "idx_demo_feedbacks_demo_created", columnList = "demo_id, created_at"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DemoFeedback {

    /** 피드백 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 피드백이 달린 데모 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demo_id", nullable = false)
    private Demo demo;

    /** 작성자 ID(users.id) */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 부모 피드백. 답글이 아니면 null */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private DemoFeedback parent;

    /** 내용 */
    @Column(nullable = false, length = 1000)
    private String content;

    /** 피드백 종류. 이 컬럼이 생기기 전의 행은 null이고 칭찬으로 읽는다 */
    @Enumerated(EnumType.STRING)
    @Column(name = "feedback_type", length = 20)
    private DemoFeedbackType type;

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
    private DemoFeedback(Demo demo, Long userId, DemoFeedback parent, String content, DemoFeedbackType type) {
        this.demo = demo;
        this.userId = userId;
        this.parent = parent;
        this.content = content;
        this.type = type;
    }

    public DemoFeedbackType getType() {
        return type == null ? DemoFeedbackType.PRAISE : type;
    }

    public void update(String content) {
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
