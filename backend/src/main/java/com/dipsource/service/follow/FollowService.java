package com.dipsource.service.follow;

/** 팔로우 관계 확인. 다른 기능(일촌평 등)에서 쓴다. */
public interface FollowService {

    /** 두 회원이 서로 팔로우 중인지(맞팔) 확인한다. 같은 회원이거나 ID가 없으면 false. */
    boolean isMutualFollow(Long userId, Long targetId);
}
