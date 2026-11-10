package com.dipsource.entity.coin;

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

/** 현금 결제(PG). 칩 충전과 시리즈 구매(이전: 블로그 구독)를 함께 기록한다. 테이블: payments */
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "payments",
        indexes = @Index(name = "idx_payments_user_created", columnList = "user_id, created_at"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    /** 결제 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 결제한 회원 ID(users.id) */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 결제 종류 */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false, length = 20)
    private PaymentType paymentType;

    /** 블로그 구독 ID(blog_subscriptions.id). BLOG_SUBSCRIPTION일 때만 사용 */
    @Column(name = "subscription_id")
    private Long subscriptionId;

    /** 시리즈 ID(post_series.id). SERIES_PURCHASE일 때만 사용 */
    @Column(name = "series_id")
    private Long seriesId;

    /** 결제 금액(원) */
    @Column(name = "amount_krw", nullable = false)
    private int amountKrw;

    /** 충전한 칩. CHIP_CHARGE일 때만 사용 */
    @Column
    private Integer chips;

    /** PG 주문 번호 */
    @Column(name = "pg_order_id", nullable = false, unique = true, length = 100)
    private String pgOrderId;

    /** 상태 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.READY;

    /** 결제 완료 시각 */
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    /** 환불 시각 */
    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    /** 환불 처리한 관리자 ID(users.id, role=ADMIN) */
    @Column(name = "refunded_by")
    private Long refundedBy;

    /** 결제 요청 시각 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private Payment(Long userId, PaymentType paymentType, Long subscriptionId, Long seriesId, int amountKrw,
                    Integer chips, String pgOrderId) {
        this.userId = userId;
        this.paymentType = paymentType;
        this.subscriptionId = subscriptionId;
        this.seriesId = seriesId;
        this.amountKrw = amountKrw;
        this.chips = chips;
        this.pgOrderId = pgOrderId;
    }

    public void markPaid() {
        this.status = PaymentStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = PaymentStatus.CANCELED;
    }

    public void refund(Long adminId) {
        this.status = PaymentStatus.REFUNDED;
        this.refundedAt = LocalDateTime.now();
        this.refundedBy = adminId;
    }
}
