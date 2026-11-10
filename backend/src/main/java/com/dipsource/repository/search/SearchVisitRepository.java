package com.dipsource.repository.search;

import com.dipsource.entity.search.SearchVisit;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface SearchVisitRepository extends Repository<SearchVisit, Long> {

    /** 방문을 원자적으로 기록한다. 같은 검색어로 같은 문서에 짧은 시간 안에 다시 들어온 것은 세지 않는다 */
    @Modifying
    @Query(nativeQuery = true, value = """
            INSERT INTO search_visits(user_id, target_type, target_id, query_norm, visit_count, last_visited_at)
            VALUES (:userId, :targetType, :targetId, :queryNorm, 1, now())
            ON CONFLICT (user_id, target_type, target_id, query_norm) DO UPDATE
            SET visit_count = search_visits.visit_count + 1, last_visited_at = now()
            WHERE search_visits.last_visited_at < :countAfter
            """)
    int recordVisit(@Param("userId") Long userId, @Param("targetType") String targetType, @Param("targetId") Long targetId,
                    @Param("queryNorm") String queryNorm, @Param("countAfter") LocalDateTime countAfter);

    @Query("""
            select v from SearchVisit v
            where v.userId = :userId and v.targetType = :targetType and v.targetId in :targetIds
              and v.visitCount >= :minVisits and v.lastVisitedAt > :after
            """)
    List<SearchVisit> findRecent(@Param("userId") Long userId, @Param("targetType") String targetType,
                                 @Param("targetIds") Collection<Long> targetIds, @Param("minVisits") int minVisits,
                                 @Param("after") LocalDateTime after);

    @Modifying
    @Query("delete from SearchVisit v where v.lastVisitedAt < :before")
    int deleteOlderThan(@Param("before") LocalDateTime before);

    @Modifying
    @Query("delete from SearchVisit v where v.userId = :userId")
    int deleteByUserId(@Param("userId") Long userId);
}
