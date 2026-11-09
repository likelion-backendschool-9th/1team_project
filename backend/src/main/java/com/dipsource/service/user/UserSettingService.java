package com.dipsource.service.user;

import com.dipsource.dto.user.UpdateUserSettingsRequest;
import com.dipsource.dto.user.UserSettingsResponse;
import com.dipsource.entity.user.UserSetting;
import com.dipsource.repository.user.UserSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 내 설정 조회·변경.
 * 지금은 값을 저장·조회만 한다. 알림 발송·프로필 공개 범위·DM 허용에 실제로 반영하는 일은 각 기능에서 이 설정을 읽어야 한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserSettingService {

    private final UserSettingRepository userSettingRepository;

    /** 저장한 적이 없으면 기본값을 돌려준다 (행은 만들지 않음) */
    public UserSettingsResponse getMine(Long userId) {
        UserSetting setting = userSettingRepository.findById(userId)
                .orElseGet(() -> UserSetting.defaults(userId));
        return UserSettingsResponse.from(setting);
    }

    @Transactional
    public UserSettingsResponse update(Long userId, UpdateUserSettingsRequest request) {
        UserSetting setting = userSettingRepository.findById(userId)
                .orElseGet(() -> userSettingRepository.save(UserSetting.defaults(userId)));
        UpdateUserSettingsRequest.Notify notify = request.notify();
        setting.update(notify.like(), notify.comment(), notify.follow(), notify.dm(), notify.guestbook(),
                request.visibility(), request.allowDm());
        return UserSettingsResponse.from(setting);
    }
}
