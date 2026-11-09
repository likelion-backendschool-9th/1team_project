package com.dipsource.repository.demo;

import com.dipsource.entity.demo.Demo;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** demos 테이블 접근 */
public interface DemoRepository extends JpaRepository<Demo, Long> {

    long countByUserIdAndDeletedAtIsNull(Long userId);

    /** 공개·버전 변경을 직렬화할 때 쓴다. 같은 데모의 동시 요청은 여기서 줄을 선다. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Demo d WHERE d.id = :id")
    Optional<Demo> findByIdForUpdate(@Param("id") Long id);

    /**
     * 데모 목록 (커서). 정렬값과 id를 묶어 비교해 같은 정렬값이 많아도 건너뛰거나 겹치지 않는다.
     * sort: latest(id) · popular(like_count) · 그 외 trending(play_count).
     * 공개 데모만 나오고, 작성자 본인이 조회하면 자기 초안도 함께 나온다.
     */
    @Query(value = """
            SELECT d.* FROM demos d
            WHERE d.deleted_at IS NULL
              AND (d.status = 'ACTIVE'
                   OR (d.status = 'DRAFT' AND d.user_id = CAST(:viewerId AS bigint) AND d.user_id = CAST(:userId AS bigint)))
              AND (CAST(:userId AS bigint) IS NULL OR d.user_id = :userId)
              AND d.user_id <> ALL(CAST(:hiddenUserIds AS bigint[]))
              AND (CAST(:tag AS text) IS NULL OR EXISTS (
                    SELECT 1 FROM demo_tags dt JOIN tags t ON t.id = dt.tag_id
                    WHERE dt.demo_id = d.id AND t.name = :tag))
              AND (CAST(:cursorId AS bigint) IS NULL
                   OR (CASE CAST(:sort AS text) WHEN 'latest' THEN d.id WHEN 'popular' THEN d.like_count ELSE d.play_count END, d.id)
                      < (:cursorValue, :cursorId))
            ORDER BY CASE CAST(:sort AS text) WHEN 'latest' THEN d.id WHEN 'popular' THEN d.like_count ELSE d.play_count END DESC, d.id DESC
            LIMIT :size
            """, nativeQuery = true)
    List<Demo> findPage(
            @Param("viewerId") Long viewerId,
            @Param("userId") Long userId,
            @Param("hiddenUserIds") Long[] hiddenUserIds,
            @Param("tag") String tag,
            @Param("sort") String sort,
            @Param("cursorValue") Long cursorValue,
            @Param("cursorId") Long cursorId,
            @Param("size") int size);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Demo d SET d.likeCount = d.likeCount + 1 WHERE d.id = :id")
    int incrementLikeCount(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Demo d SET d.likeCount = d.likeCount - 1 WHERE d.id = :id AND d.likeCount > 0")
    int decrementLikeCount(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Demo d SET d.playCount = d.playCount + 1 WHERE d.id = :id")
    int incrementPlayCount(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Demo d SET d.feedbackCount = d.feedbackCount + 1 WHERE d.id = :id")
    int incrementFeedbackCount(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Demo d SET d.feedbackCount = d.feedbackCount - 1 WHERE d.id = :id AND d.feedbackCount > 0")
    int decrementFeedbackCount(@Param("id") Long id);

    @Query("""
            SELECT d.userId, COUNT(d) FROM Demo d
            WHERE d.deletedAt IS NULL AND d.userId IN :userIds
            GROUP BY d.userId
            """)
    List<Object[]> countActiveByUserIds(@Param("userIds") Collection<Long> userIds);

    /** 관리자 데모 목록. 삭제(DELETED)만 빼고, 제목·설명·작성자 닉네임 검색. */
    @Query("""
            SELECT d FROM Demo d JOIN User u ON u.id = d.userId
            WHERE d.status <> com.dipsource.entity.demo.DemoStatus.DELETED
              AND (:q IS NULL OR LOWER(d.title) LIKE :q ESCAPE '\\'
                  OR LOWER(d.description) LIKE :q ESCAPE '\\'
                  OR LOWER(u.nickname) LIKE :q ESCAPE '\\')
            ORDER BY d.createdAt DESC, d.id DESC
            """)
    Page<Demo> searchForAdmin(@Param("q") String q, Pageable pageable);

    long countByDeletedAtIsNullAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            LocalDateTime from, LocalDateTime to);
}
