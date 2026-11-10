package com.dipsource.entity.badge;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** (user_id, badge_id) 복합 PK */
@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserBadgeId implements Serializable {

    /** users.id. user 기능의 엔티티를 직접 참조하지 않고 id만 가진다. */
    @Column(name = "user_id")
    private Long userId;

    /** 뱃지 ID */
    @Column(name = "badge_id")
    private Long badgeId;

    public UserBadgeId(Long userId, Long badgeId) {
        this.userId = userId;
        this.badgeId = badgeId;
    }
}
