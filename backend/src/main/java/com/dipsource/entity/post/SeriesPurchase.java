package com.dipsource.entity.post;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 시리즈 구매 기록. 한 사람이 한 시리즈를 한 번만 산다. 테이블: series_purchases
 * 결제(payments, SERIES_PURCHASE)와 같은 트랜잭션에서 저장한다.
 */
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "series_purchases",
        uniqueConstraints = @UniqueConstraint(name = "uk_series_purchases_user_series", columnNames = {"user_id", "series_id"}),
        indexes = @Index(name = "idx_series_purchases_series", columnList = "series_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SeriesPurchase {

    /** 구매 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 구매한 회원 ID(users.id) */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 시리즈 ID(post_series.id) */
    @Column(name = "series_id", nullable = false)
    private Long seriesId;

    /** 결제한 금액(원). 구매 이후 가격이 바뀌어도 그대로 남긴다 */
    @Column(name = "paid_price", nullable = false)
    private int paidPrice;

    /** 구매 시각 */
    @CreatedDate
    @Column(name = "purchased_at", nullable = false, updatable = false)
    private LocalDateTime purchasedAt;
}
