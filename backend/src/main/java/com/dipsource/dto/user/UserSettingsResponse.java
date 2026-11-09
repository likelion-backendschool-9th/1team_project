package com.dipsource.dto.user;

import com.dipsource.entity.user.UserSetting;

/**
 * 내 설정 (프론트 Settings). GET·PUT users/me/settings 응답.
 *
 * @param notify     알림 종류별 받기 여부
 * @param visibility 프로필 공개 범위: public / followers / private
 * @param allowDm    팔로우하지 않은 사람의 DM 허용
 */
public record UserSettingsResponse(
        Notify notify,
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
