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
        boolean onboardingIncomplete
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
                user.isOnboardingIncomplete()
        );
    }
}
