package com.dipsource.repository.minihome;

import com.dipsource.entity.minihome.MiniHome;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** mini_homes 테이블 접근. */
public interface MiniHomeRepository extends JpaRepository<MiniHome, Long> {

    Optional<MiniHome> findByUserId(Long userId);

    /** 방문 수를 한 번에 올린다. 엔티티에서 더하지 않는다. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE mini_homes
            SET today_count = today_count + 1,
                total_count = total_count + 1,
                updated_at = CURRENT_TIMESTAMP
            WHERE user_id = :userId
            """, nativeQuery = true)
    int incrementVisitCounts(@Param("userId") Long userId);

    /** 모든 미니홈피의 오늘 방문자 수를 0으로 만든다. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE mini_homes
            SET today_count = 0,
                updated_at = CURRENT_TIMESTAMP
            """, nativeQuery = true)
    int resetTodayCount();

    /**
     * 활성 회원 미니홈피 중 본인과 제외 대상을 뺀 user_id 하나.
     * 후보가 없으면 비어 있다.
     */
    @Query(value = """
            SELECT mh.user_id
            FROM mini_homes mh
            JOIN users u ON u.id = mh.user_id
            WHERE u.status = 'ACTIVE'
              AND mh.user_id <> :viewerId
              AND mh.user_id <> :excludeId
            ORDER BY random()
            LIMIT 1
            """, nativeQuery = true)
    Optional<Long> findRandomUserId(@Param("viewerId") Long viewerId, @Param("excludeId") Long excludeId);
}
