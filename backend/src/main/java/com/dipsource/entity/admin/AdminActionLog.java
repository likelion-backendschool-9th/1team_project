package com.dipsource.entity.admin;

import com.dipsource.entity.user.User;
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

/** 관리자 작업 기록. 한 번 쌓이면 바꾸지 않는다. 테이블: admin_action_logs */
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "admin_action_logs", indexes = {
        @Index(name = "idx_admin_action_logs_admin_created", columnList = "admin_id, created_at"),
        @Index(name = "idx_admin_action_logs_target", columnList = "target_type, target_id")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminActionLog {

    /** 기록 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 작업한 관리자(role=ADMIN인 회원) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;

    /** 작업 종류. 예: HIDE_FEED, SUSPEND_USER, REFUND_PAYMENT */
    @Column(name = "action_type", nullable = false, length = 30)
    private String actionType;

    /** 대상 종류. 예: FEED, POST, DEMO, USER, PAYMENT */
    @Column(name = "target_type", nullable = false, length = 20)
    private String targetType;

    /** 대상 ID */
    @Column(name = "target_id", nullable = false)
    private Long targetId;

    /** 작업 사유 */
    @Column(length = 1000)
    private String reason;

    /** 작업 시각 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private AdminActionLog(User admin, String actionType, String targetType, Long targetId, String reason) {
        this.admin = admin;
        this.actionType = actionType;
        this.targetType = targetType;
        this.targetId = targetId;
        this.reason = reason;
    }
}
