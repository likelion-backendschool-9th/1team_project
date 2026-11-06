package com.dipsource.dto.user;

import com.dipsource.entity.user.User;
import java.util.List;

/** 내 정보 조회 응답. */
public record UserMeResponse(
        Long id,
        String nickname,
        String email,
        String bio,
        String profileImage,
        String githubUsername,
        List<String> techStacks,
        boolean onboardingIncomplete,
        List<Object> badges,
        boolean plus,
        String company,
        int followerCount,
        int followingCount,
        int chipBalance
) {
    /**
     * @param followerCount  나를 팔로우하는 활동 회원 수
     * @param followingCount 내가 팔로우하는 활동 회원 수
     */
    public static UserMeResponse from(User user, long followerCount, long followingCount) {
        return new UserMeResponse(
                user.getId(),
                user.getNickname(),
                user.getEmail(),
                user.getBio(),
                user.getProfileImage(),
                user.getGithubUsername(),
                user.getTechStacks() == null ? List.of() : List.copyOf(user.getTechStacks()),
                user.isOnboardingIncomplete(),
                List.of(),   // badges — 아직 미구현
                false,       // plus
                null,        // company
                Math.toIntExact(followerCount),
                Math.toIntExact(followingCount),
                user.getChipBalance()
        );
    }
}
