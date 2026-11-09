package com.dipsource.repository.demo;

import com.dipsource.entity.demo.DemoUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * demo_usages 테이블 접근.
 * "합계를 읽고 비교한 뒤 쓴다"는 동시 요청에 뚫린다. 조건까지 UPDATE 한 문장에 넣어 행 잠금 안에서 판단한다.
 */
public interface DemoUsageRepository extends JpaRepository<DemoUsage, Long> {

    /** 행이 없으면 지금까지의 실제 사용량으로 만든다. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            INSERT INTO demo_usages (user_id, used_bytes, reserved_bytes, demo_count)
            VALUES (:userId,
                    COALESCE((SELECT SUM(v.charged_bytes) FROM demo_versions v JOIN demos d ON d.id = v.demo_id
                              WHERE d.user_id = :userId AND v.status = 'READY' AND d.deleted_at IS NULL), 0),
                    0,
                    (SELECT COUNT(*) FROM demos d WHERE d.user_id = :userId AND d.deleted_at IS NULL))
            ON CONFLICT (user_id) DO NOTHING
            """, nativeQuery = true)
    int ensureRow(@Param("userId") Long userId);

    /** @return 1이면 예약 성공, 0이면 한도 초과 */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE demo_usages SET reserved_bytes = reserved_bytes + :bytes
            WHERE user_id = :userId AND used_bytes + reserved_bytes + :bytes <= :limit
            """, nativeQuery = true)
    int tryReserve(@Param("userId") Long userId, @Param("bytes") long bytes, @Param("limit") long limit);

    /** 예약을 실제 사용량으로 옮긴다. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE demo_usages SET reserved_bytes = GREATEST(reserved_bytes - :bytes, 0), used_bytes = used_bytes + :bytes
            WHERE user_id = :userId
            """, nativeQuery = true)
    int commitReservation(@Param("userId") Long userId, @Param("bytes") long bytes);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE demo_usages SET reserved_bytes = GREATEST(reserved_bytes - :bytes, 0) WHERE user_id = :userId",
            nativeQuery = true)
    int releaseReservation(@Param("userId") Long userId, @Param("bytes") long bytes);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE demo_usages SET used_bytes = GREATEST(used_bytes - :bytes, 0) WHERE user_id = :userId",
            nativeQuery = true)
    int releaseUsed(@Param("userId") Long userId, @Param("bytes") long bytes);

    /** @return 1이면 성공, 0이면 데모 수 한도 초과 */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE demo_usages SET demo_count = demo_count + 1 WHERE user_id = :userId AND demo_count < :max",
            nativeQuery = true)
    int tryIncrementDemoCount(@Param("userId") Long userId, @Param("max") int max);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE demo_usages SET demo_count = GREATEST(demo_count - 1, 0) WHERE user_id = :userId", nativeQuery = true)
    int decrementDemoCount(@Param("userId") Long userId);
}
