package com.dipsource.repository.user;

import com.dipsource.entity.user.User;
import com.dipsource.entity.user.enums.UserRole;
import com.dipsource.entity.user.enums.UserStatus;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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

    /** 제재·해제 시 같은 회원을 동시에 바꾸지 못하게 잠근다 */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") Long id);

    /**
     * 딥소스 플러스 자동 갱신 대상. 만료 시각이 지났고 자동 갱신이 켜진 활동 회원이다.
     * 해지했거나 탈퇴·정지한 회원은 빠진다.
     */
    @Query("""
            SELECT u.id FROM User u
            WHERE u.plusAutoRenew = true
              AND u.plusUntil IS NOT NULL
              AND u.plusUntil <= :now
              AND u.status = com.dipsource.entity.user.enums.UserStatus.ACTIVE
            """)
    List<Long> findPlusRenewalDueIds(@Param("now") LocalDateTime now);

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

    /**
     * 권한 관리 화면용 회원 목록. 탈퇴 회원은 빼고, 마스터·관리자를 먼저 보여 준다.
     * q는 소문자 LIKE 패턴(%...%, \\로 이스케이프)이고 null이면 거르지 않는다.
     */
    @Query("""
        SELECT u FROM User u
        WHERE u.status <> com.dipsource.entity.user.enums.UserStatus.DELETED
        AND u.role IN :roles
        AND (:q IS NULL OR LOWER(u.nickname) LIKE :q ESCAPE '\\'
            OR LOWER(u.email) LIKE :q ESCAPE '\\')
        ORDER BY CASE u.role
            WHEN com.dipsource.entity.user.enums.UserRole.MASTER THEN 0
            WHEN com.dipsource.entity.user.enums.UserRole.ADMIN THEN 1
            ELSE 2 END,
            u.id ASC
        """)
    Page<User> searchForRoles(
        @Param("roles") List<UserRole> roles,
        @Param("q") String q,
        Pageable pageable);

    long countByStatusNot(UserStatus status);

    long countByStatusNotAndCreatedAtGreaterThanEqual(UserStatus status, LocalDateTime from);

    /**
     * 요일별 가입 수. created_at은 timestamp(without time zone)이고 서울 벽시계로 저장하므로
     * CAST AS date는 세션 시간대와 무관하게 서울 달력 날짜다.
     */
    @Query(value = """
            SELECT CAST(created_at AS date) AS day, COUNT(*)
            FROM users
            WHERE created_at >= :from AND created_at < :to
              AND status <> 'DELETED'
            GROUP BY CAST(created_at AS date)
            """, nativeQuery = true)
    List<Object[]> countSignupsByDay(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
}
