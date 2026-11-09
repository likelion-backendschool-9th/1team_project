package com.dipsource.dto.user;

import com.dipsource.entity.user.UserSetting;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 내 설정 (프론트 Settings). GET·PUT users/me/settings 응답.
 *
 * @param notifications 알림 종류별 받기 여부. JSON 키는 notify (notify는 Object의 메서드 이름이라 레코드 필드로 못 쓴다)
 * @param visibility 프로필 공개 범위: public / followers / private
 * @param allowDm    팔로우하지 않은 사람의 DM 허용
 */
public record UserSettingsResponse(
        @JsonProperty("notify") Notify notifications,
        String visibility,
        boolean allowDm
) {

    public record Notify(boolean like, boolean comment, boolean follow, boolean dm, boolean guestbook) {
    }

    public static UserSettingsResponse from(UserSetting setting) {
        return new UserSettingsResponse(
                new Notify(setting.isNotifyLike(), setting.isNotifyComment(), setting.isNotifyFollow(),
                        setting.isNotifyDm(), setting.isNotifyGuestbook()),
                setting.getVisibility(),
                setting.isAllowDm());
    }
}
