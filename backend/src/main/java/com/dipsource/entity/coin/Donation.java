package com.dipsource.entity.coin;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
 * 미니홈피 후원. 테이블: donations
 * 후원한 칩 수는 이 테이블에 없고 chip_transactions(ref_type = DONATION, ref_id = 이 ID)에 남는다.
 */
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "donations",
        indexes = @Index(name = "idx_donations_home_created", columnList = "mini_home_id, created_at"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Donation {

    /** 후원 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 후원한 회원 ID(users.id) */
    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    /** 후원받은 미니홈피 ID(mini_homes.id) */
    @Column(name = "mini_home_id", nullable = false)
    private Long miniHomeId;

    /** 후원 시각 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private Donation(Long senderId, Long miniHomeId) {
        this.senderId = senderId;
        this.miniHomeId = miniHomeId;
    }
}
