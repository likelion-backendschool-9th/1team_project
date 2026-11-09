package com.dipsource.repository.demo;

import com.dipsource.entity.demo.DemoFeedback;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** demo_feedbacks 테이블 접근 */
public interface DemoFeedbackRepository extends JpaRepository<DemoFeedback, Long> {

    /** 표시할 최상위 피드백 (최신순, 커서). 삭제됐고 활성 답글도 없는 것과 숨길 회원의 글은 DB에서 뺀다 */
    @Query(value = """
            SELECT * FROM demo_feedbacks f
            WHERE f.demo_id = :demoId
              AND f.parent_id IS NULL
              AND (CAST(:cursor AS bigint) IS NULL OR f.id < :cursor)
              AND f.user_id <> ALL(CAST(:hiddenUserIds AS bigint[]))
              AND (f.deleted_at IS NULL
                   OR EXISTS (
                       SELECT 1 FROM demo_feedbacks r
                       WHERE r.parent_id = f.id AND r.deleted_at IS NULL
                         AND r.user_id <> ALL(CAST(:hiddenUserIds AS bigint[]))
                   ))
            ORDER BY f.id DESC
            LIMIT :size
            """, nativeQuery = true)
    List<DemoFeedback> findTopLevelVisible(
            @Param("demoId") Long demoId,
            @Param("cursor") Long cursor,
            @Param("hiddenUserIds") Long[] hiddenUserIds,
            @Param("size") int size);

    /** 여러 피드백의 답글 (N+1 방지) */
    @Query(value = """
            SELECT * FROM demo_feedbacks
            WHERE parent_id IN (:parentIds) AND deleted_at IS NULL
            ORDER BY id ASC
            """, nativeQuery = true)
    List<DemoFeedback> findRepliesByParentIds(@Param("parentIds") List<Long> parentIds);

    /** 동시 삭제에도 카운트가 한 번만 줄도록 조건부로 지운다 — 실제 변경된 행 수 반환 */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE DemoFeedback f SET f.deletedAt = CURRENT_TIMESTAMP WHERE f.id = :id AND f.deletedAt IS NULL")
    int softDeleteIfActive(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE DemoFeedback f SET f.likeCount = f.likeCount + 1 WHERE f.id = :id")
    int incrementLikeCount(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE DemoFeedback f SET f.likeCount = f.likeCount - 1 WHERE f.id = :id AND f.likeCount > 0")
    int decrementLikeCount(@Param("id") Long id);
}
