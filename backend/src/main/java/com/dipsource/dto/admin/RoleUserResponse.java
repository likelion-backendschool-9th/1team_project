package com.dipsource.dto.admin;

import com.dipsource.entity.user.User;

/**
 * 권한 관리 화면의 회원 한 명.
 *
 * @param role USER / ADMIN / MASTER
 */
public record RoleUserResponse(
        Long id,
        String nickname,
        String email,
        String profileImage,
        String role
) {

    public static RoleUserResponse from(User user) {
        return new RoleUserResponse(
                user.getId(),
                user.getNickname(),
                user.getEmail(),
                user.getProfileImage(),
                user.getRole().name());
    }
}
