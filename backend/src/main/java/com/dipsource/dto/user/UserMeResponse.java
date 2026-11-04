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
    public static UserMeResponse from(User user) {
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
                0,           // followerCount — 추후 팔로우 테이블에서 조회
                0,           // followingCount
                0            // chipBalance
        );
    }
}
