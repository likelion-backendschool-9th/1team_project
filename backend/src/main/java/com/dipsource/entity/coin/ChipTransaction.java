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

/**
 * 칩 거래 내역. 한 번 쌓이면 바꾸지 않는다. 테이블: chip_transactions
 * users.chip_balance 변경과 반드시 같은 트랜잭션에서 저장한다.
 */
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "chip_transactions",
        indexes = @Index(name = "idx_chip_transactions_user_created", columnList = "user_id, created_at"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChipTransaction {

    /** 거래 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 회원 ID(users.id) */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 거래 종류 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChipTransactionType type;

    /** 증감한 칩. 늘면 양수, 줄면 음수 */
    @Column(nullable = false)
    private int amount;

    /** 거래 후 잔액 */
    @Column(name = "balance_after", nullable = false)
    private int balanceAfter;

    /** 원본 기록 종류 */
    @Enumerated(EnumType.STRING)
    @Column(name = "ref_type", length = 30)
    private ChipRefType refType;

    /** 원본 기록 ID. ref_type이 가리키는 테이블의 ID */
    @Column(name = "ref_id")
    private Long refId;

    /** 설명 */
    @Column(length = 200)
    private String description;

    /** 조정한 관리자 ID(admins.id). ADMIN_ADJUST일 때만 사용 */
    @Column(name = "admin_id")
    private Long adminId;

    /** 거래 시각 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private ChipTransaction(Long userId, ChipTransactionType type, int amount, int balanceAfter,
                            ChipRefType refType, Long refId, String description, Long adminId) {
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.refType = refType;
        this.refId = refId;
        this.description = description;
        this.adminId = adminId;
    }
}
