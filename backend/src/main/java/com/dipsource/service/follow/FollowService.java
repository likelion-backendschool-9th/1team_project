package com.dipsource.service.follow;

import com.dipsource.dto.follow.FollowPresenceResponse;
import com.dipsource.dto.follow.FollowUserResponse;
import com.dipsource.global.common.PageResponse;
import java.util.List;
import org.springframework.data.domain.Pageable;

/** 팔로우·언팔로우와 팔로우 관계 확인. */
public interface FollowService {

    /** 팔로우. 이미 팔로우 중이어도 오류 없이 넘어간다 (멱등). 나 자신이거나 활동 중이 아닌 회원이면 예외. */
    void follow(Long followerId, Long targetId);

    /** 언팔로우. 팔로우 중이 아니어도 오류 없이 넘어간다 (멱등). 나 자신이면 예외. */
    void unfollow(Long followerId, Long targetId);

    /** 내가 팔로우하는 회원 목록. 최근에 팔로우한 순, 활동 중인 회원만. */
    PageResponse<FollowUserResponse> getFollowings(Long userId, Pageable pageable);

    /** 나를 팔로우하는 회원 목록. 최근에 팔로우한 순, 활동 중인 회원만. 맞팔 여부를 함께 준다. */
    PageResponse<FollowUserResponse> getFollowers(Long userId, Pageable pageable);

    /**
     * userIds 가운데 내가 팔로우 중인 활동 회원의 접속 상태. DM과 같은 기준(최근 활동 시각)으로 판단한다.
     * 화면에 나온 아이디만 넘기며, 최대 100개까지 본다. 팔로우하지 않거나 활동 중이 아닌 회원은 빠진다.
     */
    List<FollowPresenceResponse> getFollowingPresence(Long userId, List<Long> userIds);

    /** 두 회원이 서로 팔로우 중인지(맞팔) 확인한다. 같은 회원이거나 ID가 없으면 false. */
    boolean isMutualFollow(Long userId, Long targetId);
}
