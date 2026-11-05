package com.dipsource.dto.follow;

import com.dipsource.dto.user.UserSummaryResponse;
import java.util.List;

/**
 * 팔로잉·팔로워 목록의 회원 한 명.
 *
 * @param id           회원 ID
 * @param nickname     닉네임
 * @param profileImage 프로필 이미지 URL
 * @param techStacks   기술 스택
 * @param badges       뱃지 (뱃지 기능 전이라 빈 목록)
 * @param following    내가 이 회원을 팔로우 중인지
 * @param followsMe    이 회원이 나를 팔로우 중인지
 */
public record FollowUserResponse(
        Long id,
        String nickname,
        String profileImage,
        List<String> techStacks,
        List<Object> badges,
        boolean following,
        boolean followsMe
) {

    public static FollowUserResponse of(UserSummaryResponse user, boolean following, boolean followsMe) {
        return new FollowUserResponse(
                user.id(),
                user.nickname(),
                user.profileImage(),
                user.techStacks(),
                List.of(),   // badges — 아직 미구현
                following,
                followsMe
        );
    }
}
