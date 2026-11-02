package com.dipsource.entity.follow;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** 팔로우. 회원은 user 기능이라 id만 가진다. 테이블: follows */
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "follows",
        indexes = @Index(name = "idx_follows_following", columnList = "following_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Follow {

    /** 팔로워·팔로잉 회원 ID 복합 키 */
    @EmbeddedId
    private FollowId id;

    /** 팔로우한 시각 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Follow(Long followerId, Long followingId) {
        this.id = new FollowId(followerId, followingId);
    }
}
