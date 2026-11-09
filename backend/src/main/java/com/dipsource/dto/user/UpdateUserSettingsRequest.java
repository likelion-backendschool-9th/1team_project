package com.dipsource.dto.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * 내 설정 변경 요청. PUT users/me/settings
 *
 * @param notify     알림 종류별 받기 여부
 * @param visibility public / followers / private
 * @param allowDm    팔로우하지 않은 사람의 DM 허용
 */
public record UpdateUserSettingsRequest(
        @NotNull @Valid Notify notify,
        @NotNull @Pattern(regexp = "public|followers|private", message = "공개 범위를 다시 골라 주세요.") String visibility,
        @NotNull Boolean allowDm
) {

    public record Notify(
            @NotNull Boolean like,
            @NotNull Boolean comment,
            @NotNull Boolean follow,
            @NotNull Boolean dm,
            @NotNull Boolean guestbook
    ) {
    }
}
