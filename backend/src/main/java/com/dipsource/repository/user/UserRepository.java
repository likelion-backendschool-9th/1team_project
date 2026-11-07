package com.dipsource.repository.user;

import com.dipsource.entity.user.User;
import com.dipsource.entity.user.enums.UserStatus;
import java.util.Optional;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** users 테이블 접근. */
public interface UserRepository extends JpaRepository<User, Long> {

    /** 닉네임 중복 여부 확인 */
    boolean existsByNickname(String nickname);

    /** 본인을 제외한 닉네임 중복 여부 */
    boolean existsByNicknameAndIdNot(String nickname, Long id);

    /** 이메일 중복 여부 확인 */
    boolean existsByEmail(String email);

    /** 해당 상태의 회원이 있는지 확인. 예: 활동 중(ACTIVE)인 회원인지 */
    boolean existsByIdAndStatus(Long id, UserStatus status);

    /** 이메일로 회원 조회 */
    Optional<User> findByEmail(String email);

    @Query("""
        SELECT u FROM User u
        WHERE (:status IS NULL OR u.status = :status)
        AND (:q IS NULL OR LOWER(u.nickname) LIKE :q ESCAPE '\\'
            OR LOWER(u.email) LIKE :q ESCAPE '\\')
        ORDER BY u.createdAt DESC, u.id DESC
        """)
    Page<User> searchForAdmin(
        @Param("status") UserStatus status,
        @Param("q") String q,
        Pageable pageable);
}
