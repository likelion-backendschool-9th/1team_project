package com.dipsource.dto.block;

import com.dipsource.entity.user.User;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** 내가 차단한 회원. 설정 화면 목록용. */
public record BlockedUserResponse(
        Long id,
        String nickname,
        String profileImage,
        List<String> techStacks,
        String blockedAt) {

    public static BlockedUserResponse of(User user, LocalDateTime blockedAt) {
        return new BlockedUserResponse(
                user.getId(),
                user.getDisplayNickname(),
                user.isWithdrawn() ? null : user.getProfileImage(),
                user.getTechStacks() == null ? List.of() : List.copyOf(user.getTechStacks()),
                blockedAt == null ? null : blockedAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }
}
