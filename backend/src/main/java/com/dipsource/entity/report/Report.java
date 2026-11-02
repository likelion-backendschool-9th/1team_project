package com.dipsource.entity.report;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** 신고. 대상은 여러 기능에 걸쳐 있어 종류(target_type)와 ID로만 가리킨다. 테이블: reports */
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "reports", indexes = {
        @Index(name = "idx_reports_status_created", columnList = "status, created_at"),
        @Index(name = "idx_reports_target", columnList = "target_type, target_id")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report {

    /** 신고 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 신고한 회원 ID(users.id) */
    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;

    /** 대상 종류. 예: FEED, POST, DEMO, FEED_COMMENT, GUESTBOOK, USER */
    @Column(name = "target_type", nullable = false, length = 20)
    private String targetType;

    /** 대상 ID */
    @Column(name = "target_id", nullable = false)
    private Long targetId;

    /** 신고 사유 */
    @Column(nullable = false, length = 500)
    private String reason;

    /** 처리 상태 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status = ReportStatus.PENDING;

    /** 처리한 관리자 ID(admins.id) */
    @Column(name = "handled_by")
    private Long handledBy;

    /** 관리자 메모 */
    @Column(name = "admin_note", length = 1000)
    private String adminNote;

    /** 신고 시각 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 처리 시각 */
    @Column(name = "handled_at")
    private LocalDateTime handledAt;

    @Builder
    private Report(Long reporterId, String targetType, Long targetId, String reason) {
        this.reporterId = reporterId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.reason = reason;
    }

    public void resolve(Long adminId, String adminNote) {
        handle(ReportStatus.RESOLVED, adminId, adminNote);
    }

    public void reject(Long adminId, String adminNote) {
        handle(ReportStatus.REJECTED, adminId, adminNote);
    }

    private void handle(ReportStatus status, Long adminId, String adminNote) {
        this.status = status;
        this.handledBy = adminId;
        this.adminNote = adminNote;
        this.handledAt = LocalDateTime.now();
    }
}
