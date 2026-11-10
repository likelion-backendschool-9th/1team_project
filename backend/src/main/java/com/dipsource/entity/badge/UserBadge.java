package com.dipsource.entity.badge;

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

/** 회원이 획득한 뱃지. 테이블: user_badges */
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "user_badges")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserBadge {

    /** 회원 ID·뱃지 ID 복합 키 */
    @EmbeddedId
    private UserBadgeId id;

    /** 획득한 뱃지 */
    @MapsId("badgeId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "badge_id", nullable = false)
    private Badge badge;

    /** 프로필에 보여줄지 여부 */
    @Column(name = "is_displayed", nullable = false)
    private boolean displayed = false;

    /** 프로필에 보여 줄 순서 */
    @Column(name = "display_order", nullable = false)
    private int displayOrder = 0;

    /** 획득 시각 */
    @CreatedDate
    @Column(name = "acquired_at", nullable = false, updatable = false)
    private LocalDateTime acquiredAt;

    public UserBadge(Long userId, Badge badge) {
        this.id = new UserBadgeId(userId, badge.getId());
        this.badge = badge;
    }

    public void display(int order) {
        this.displayed = true;
        this.displayOrder = order;
    }

    public void hide() {
        this.displayed = false;
    }
}
