package com.dipsource.entity.user;

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

/** 회원이 직접 고른 관심 태그. 테이블: user_interest_tags */
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "user_interest_tags")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserInterestTag {

    /** 회원 ID·태그 ID 복합 키 */
    @EmbeddedId
    private UserTagId id;

    /** 관심 태그를 고른 회원 */
    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 고른 시각 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public UserInterestTag(User user, Long tagId) {
        this.id = new UserTagId(user.getId(), tagId);
        this.user = user;
    }
}
