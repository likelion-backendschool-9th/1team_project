package com.dipsource.entity.follow;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** (follower_id, following_id) 복합 PK */
@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FollowId implements Serializable {

    /** 팔로우하는 회원 ID(users.id) */
    @Column(name = "follower_id")
    private Long followerId;

    /** 팔로우받는 회원 ID(users.id) */
    @Column(name = "following_id")
    private Long followingId;

    public FollowId(Long followerId, Long followingId) {
        this.followerId = followerId;
        this.followingId = followingId;
    }
}
