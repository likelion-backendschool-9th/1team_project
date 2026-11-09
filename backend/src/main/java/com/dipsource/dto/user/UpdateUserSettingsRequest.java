package com.dipsource.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * 내 설정 변경 요청. PUT users/me/settings
 *
 * @param notifications 알림 종류별 받기 여부. JSON 키는 notify (notify는 Object의 메서드 이름이라 레코드 필드로 못 쓴다)
 * @param visibility public / followers / private
 * @param allowDm    팔로우하지 않은 사람의 DM 허용
 */
public record UpdateUserSettingsRequest(
        @JsonProperty("notify") @NotNull @Valid Notify notifications,
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
