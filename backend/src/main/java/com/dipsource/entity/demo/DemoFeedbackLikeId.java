package com.dipsource.entity.demo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** (user_id, feedback_id) 복합 PK */
@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DemoFeedbackLikeId implements Serializable {

    /** 회원 ID(users.id) */
    @Column(name = "user_id")
    private Long userId;

    /** 데모 피드백 ID */
    @Column(name = "feedback_id")
    private Long feedbackId;

    public DemoFeedbackLikeId(Long userId, Long feedbackId) {
        this.userId = userId;
        this.feedbackId = feedbackId;
    }
}
