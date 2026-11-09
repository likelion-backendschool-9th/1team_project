package com.dipsource.repository.user;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dipsource.entity.user.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    Optional<RefreshToken> findByJti(String jti);

    /**
     * 같은 family의 갱신·폐기를 한 트랜잭션씩만 진행한다.
     * 행 잠금만 있으면 다른 토큰 행의 INSERT가 폐기 UPDATE에 안 잡힐 수 있다.
     */
    @Query(value = "SELECT pg_advisory_xact_lock(hashtextextended(:familyId, 0))", nativeQuery = true)
    void lockFamily(@Param("familyId") String familyId);

    @Modifying
    @Query("update RefreshToken t set t.revokedAt = :now where t.familyId = :familyId and t.revokedAt is null")
    int revokeFamily(@Param("familyId") String familyId, @Param("now") LocalDateTime now);

    @Query("select distinct t.familyId from RefreshToken t where t.userId = :userId")
    List<String> findDistinctFamilyIdsByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("update RefreshToken t set t.revokedAt = :now where t.userId = :userId and t.revokedAt is null")
    int revokeAllByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    int deleteByExpiresAtBefore(LocalDateTime cutoff);
}