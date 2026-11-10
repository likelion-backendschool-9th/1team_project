package com.dipsource.repository.user;

import com.dipsource.entity.user.UserSetting;
import org.springframework.data.jpa.repository.JpaRepository;

/** user_settings 테이블 접근 */
public interface UserSettingRepository extends JpaRepository<UserSetting, Long> {
}
