package com.dipsource.repository.search;

import com.dipsource.entity.search.SearchHiddenResult;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface SearchHiddenResultRepository extends Repository<SearchHiddenResult, Long> {

    /** 이미 숨겼으면 아무 일도 하지 않는다 */
    @Modifying
    @Query(nativeQuery = true, value = """
            INSERT INTO search_hidden_results(user_id, target_type, target_id, created_at)
            VALUES (:userId, :targetType, :targetId, now())
            ON CONFLICT (user_id, target_type, target_id) DO NOTHING
            """)
    int insertIgnore(@Param("userId") Long userId, @Param("targetType") String targetType, @Param("targetId") Long targetId);

    @Modifying
    @Query("delete from SearchHiddenResult h where h.userId = :userId and h.targetType = :targetType and h.targetId = :targetId")
    int delete(@Param("userId") Long userId, @Param("targetType") String targetType, @Param("targetId") Long targetId);

    /** 검색 제외용. 최근 것부터 limit개 */
    @Query(nativeQuery = true, value = """
            SELECT target_id FROM search_hidden_results
            WHERE user_id = :userId AND target_type = :targetType
            ORDER BY id DESC LIMIT :limit
            """)
    List<Long> findTargetIds(@Param("userId") Long userId, @Param("targetType") String targetType, @Param("limit") int limit);

    Page<SearchHiddenResult> findByUserIdOrderByIdDesc(Long userId, Pageable pageable);

    @Modifying
    @Query("delete from SearchHiddenResult h where h.userId = :userId")
    int deleteByUserId(@Param("userId") Long userId);
}
