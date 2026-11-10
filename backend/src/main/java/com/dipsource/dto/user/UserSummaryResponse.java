package com.dipsource.dto.user;

import com.dipsource.entity.user.User;
import java.util.List;

/**
 * 다른 화면(DM·피드 등)에 함께 보여 줄 회원 요약.
 *
 * @param id           회원 ID
 * @param nickname     닉네임. 탈퇴한 회원이면 "삭제된 유저"
 * @param profileImage 프로필 이미지 URL
 * @param techStacks   기술 스택
 */
public record UserSummaryResponse(
        Long id,
        String nickname,
        String profileImage,
        List<String> techStacks
) {

    public static UserSummaryResponse from(User user) {
        // DB에서 유저 레코드가 없을 때(하드 삭제 등) null이 넘어올 수 있으므로 방어 처리
        if (user == null) {
            return new UserSummaryResponse(null, "삭제된 유저", null, List.of());
        }
        return new UserSummaryResponse(
                user.getId(),
                user.getDisplayNickname(),
                user.isWithdrawn() ? null : user.getProfileImage(),
                user.getTechStacks() == null ? List.of() : List.copyOf(user.getTechStacks())
        );
    }
}
