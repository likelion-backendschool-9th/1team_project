package com.dipsource.entity.post;

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

/**
 * 유료 글 구매 기록. 테이블: post_purchases
 * 칩 차감(users.chip_balance), chip_transactions 기록과 같은 트랜잭션에서 저장한다.
 */
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "post_purchases")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostPurchase {

    /** 회원 ID·글 ID 복합 키 */
    @EmbeddedId
    private PostUserId id;

    /** 구매한 글 */
    @MapsId("postId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    /** 결제한 칩. 구매 이후 글 가격이 바뀌어도 그대로 남긴다 */
    @Column(name = "paid_chips", nullable = false)
    private int paidChips;

    /** 구매 시각 */
    @CreatedDate
    @Column(name = "purchased_at", nullable = false, updatable = false)
    private LocalDateTime purchasedAt;

    public PostPurchase(Long userId, Post post, int paidChips) {
        this.id = new PostUserId(userId, post.getId());
        this.post = post;
        this.paidChips = paidChips;
    }
}
