package com.dipsource.repository.user;

import com.dipsource.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

/** users 테이블 접근. */
public interface UserRepository extends JpaRepository<User, Long> {

    /** 닉네임 중복 여부 확인 */
    boolean existsByNickname(String nickname);

    /** 이메일 중복 여부 확인 */
    boolean existsByEmail(String email);
}
