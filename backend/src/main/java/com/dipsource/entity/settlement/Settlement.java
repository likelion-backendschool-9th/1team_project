package com.dipsource.entity.settlement;

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
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** 창작자 정산(칩 → 현금). 테이블: settlements */
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "settlements",
        indexes = @Index(name = "idx_settlements_user_period", columnList = "user_id, period_start"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Settlement {

    /** 정산 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 정산받는 회원 ID(users.id) */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 정산 대상 칩 합계 */
    @Column(name = "total_chips", nullable = false)
    private int totalChips;

    /** 칩을 원으로 바꾼 금액 */
    @Column(name = "amount_krw", nullable = false)
    private int amountKrw;

    /** 수수료(원) */
    @Column(nullable = false)
    private int fee;

    /** 실지급액(원) = amount_krw - fee */
    @Column(name = "net_amount", nullable = false)
    private int netAmount;

    /** 정산 기간 시작일 */
    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    /** 정산 기간 종료일 */
    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    /** 상태 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SettlementStatus status = SettlementStatus.PENDING;

    /** 확정한 관리자 ID(users.id, role=ADMIN) */
    @Column(name = "confirmed_by")
    private Long confirmedBy;

    /** 확정 시각 */
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    /** 정산 생성 시각 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private Settlement(Long userId, int totalChips, int amountKrw, int fee,
                       LocalDate periodStart, LocalDate periodEnd) {
        this.userId = userId;
        this.totalChips = totalChips;
        this.amountKrw = amountKrw;
        this.fee = fee;
        this.netAmount = amountKrw - fee;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
    }

    public void confirm(Long adminId) {
        this.status = SettlementStatus.CONFIRMED;
        this.confirmedBy = adminId;
        this.confirmedAt = LocalDateTime.now();
    }
}
