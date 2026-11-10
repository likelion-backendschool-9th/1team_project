package com.dipsource.repository.user;

import com.dipsource.entity.user.UserSanction;
import com.dipsource.entity.user.enums.UserStatus;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

public interface UserSanctionRepository extends JpaRepository<UserSanction, Long> {

    List<UserSanction> findByUser_IdOrderByCreatedAtDesc(Long userId);
    
      /** 아직 해제하지 않은 제재. 여러 건이면 가장 최근 것. */
    Optional<UserSanction> findFirstByUser_IdAndReleasedAtIsNullOrderByCreatedAtDesc(Long userId);

    /** 기간이 지났는데 아직 해제되지 않은 제재. 영구(endsAt null)는 제외. */
    @Query("""
            SELECT s FROM UserSanction s JOIN FETCH s.user u
            WHERE s.releasedAt IS NULL
            AND s.endsAt IS NOT NULL
            AND s.endsAt <= :now
            AND u.status = :status
            """)
    List<UserSanction> findExpiredActive(
            @Param("now") LocalDateTime now,
            @Param("status") UserStatus status);
}
