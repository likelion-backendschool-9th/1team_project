package com.dipsource.dto.follow;

/**
 * 팔로잉 회원의 접속 상태.
 *
 * @param userId 회원 ID
 * @param online 최근 활동이 있어 접속 중으로 보는지
 */
public record FollowPresenceResponse(
        Long userId,
        boolean online
) {
}
