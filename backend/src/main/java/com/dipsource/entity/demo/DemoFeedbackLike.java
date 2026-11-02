package com.dipsource.entity.demo;

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

/** 데모 피드백 좋아요. 테이블: demo_feedback_likes */
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "demo_feedback_likes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DemoFeedbackLike {

    /** 회원 ID·피드백 ID 복합 키 */
    @EmbeddedId
    private DemoFeedbackLikeId id;

    /** 대상 피드백 */
    @MapsId("feedbackId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feedback_id", nullable = false)
    private DemoFeedback feedback;

    /** 좋아요한 시각 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public DemoFeedbackLike(Long userId, DemoFeedback feedback) {
        this.id = new DemoFeedbackLikeId(userId, feedback.getId());
        this.feedback = feedback;
    }
}
