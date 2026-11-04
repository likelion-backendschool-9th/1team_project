package com.dipsource.dto.user;

import com.dipsource.entity.user.User;
import java.util.List;

/**
 * 검색 결과의 회원 한 명.
 *
 * @param id           회원 ID
 * @param nickname     닉네임
 * @param profileImage 프로필 이미지 URL
 * @param bio          자기소개
 * @param techStacks   기술 스택
 */
public record UserSearchItemResponse(
        Long id,
        String nickname,
        String profileImage,
        String bio,
        List<String> techStacks
) {

    public static UserSearchItemResponse from(User user) {
        return new UserSearchItemResponse(
                user.getId(),
                user.getNickname(),
                user.getProfileImage(),
                user.getBio(),
                user.getTechStacks() == null ? List.of() : List.copyOf(user.getTechStacks())
        );
    }
}
