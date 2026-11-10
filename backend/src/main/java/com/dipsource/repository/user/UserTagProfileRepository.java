package com.dipsource.repository.user;

import com.dipsource.entity.user.UserTagId;
import com.dipsource.entity.user.UserTagProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTagProfileRepository extends JpaRepository<UserTagProfile, UserTagId> {

    /** 사용자의 활동 데이터(태그 가중치)가 있는지 확인 */
    boolean existsByIdUserId(Long userId);
}
