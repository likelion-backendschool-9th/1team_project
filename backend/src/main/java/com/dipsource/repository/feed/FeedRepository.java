package com.dipsource.repository.feed;

import com.dipsource.entity.feed.Feed;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FeedRepository extends JpaRepository<Feed, Long> {

    /**
     * 점수 기반 추천 피드 조회 (커서 지원).
     * cursorId가 null이면 첫 페이지, 있으면 해당 ID 이후부터 조회.
     */
    @Query(nativeQuery = true, value = """
        SELECT f.*,
            (10
             + CASE WHEN f.user_id = ANY(:followingIds) THEN 50 ELSE 0 END
             + f.like_count
             + f.comment_count * 2
             + f.repost_count * 3
             - EXTRACT(EPOCH FROM (NOW() - f.created_at)) / 3600
             + COALESCE(tm.tag_match_score, 0)
            ) AS score
        FROM feeds f
        LEFT JOIN (
            SELECT ft.feed_id,
                   LEAST(SUM(tp.weight) * 5, 20) AS tag_match_score
            FROM feed_tags ft
            JOIN user_tag_profile tp
                 ON ft.tag_id = tp.tag_id AND tp.user_id = :userId
            GROUP BY ft.feed_id
        ) tm ON f.id = tm.feed_id
        WHERE f.status = 'ACTIVE'
          AND f.created_at > NOW() - INTERVAL '7 days'
          AND f.user_id != :userId
          AND f.user_id NOT IN (
              SELECT blocked_id FROM blocks WHERE blocker_id = :userId
              UNION
              SELECT blocker_id FROM blocks WHERE blocked_id = :userId
          )
          AND (:cursorId IS NULL OR f.id < :cursorId)
        ORDER BY score DESC, f.id DESC
        LIMIT :size
        """)
    List<Feed> findRecommendedFeeds(
            @Param("userId") Long userId,
            @Param("followingIds") Long[] followingIds,
            @Param("cursorId") Long cursorId,
            @Param("size") int size
    );


    /**
     * 팔로잉 피드 조회 (커서 지원).
     */
    @Query(nativeQuery = true, value = """
    SELECT f.*
    FROM feeds f
    WHERE f.status = 'ACTIVE'
      AND f.user_id = ANY(:followingIds)
      AND (:userId IS NULL OR f.user_id NOT IN (
          SELECT blocked_id FROM blocks WHERE blocker_id = :userId
          UNION
          SELECT blocker_id FROM blocks WHERE blocked_id = :userId
      ))
      AND (:cursorId IS NULL OR f.id < :cursorId)
    ORDER BY f.id DESC
    LIMIT :size
    """)
    List<Feed> findFollowingFeeds(
            @Param("userId") Long userId,
            @Param("followingIds") Long[] followingIds,
            @Param("cursorId") Long cursorId,
            @Param("size") int size
    );

    /**
     * 지식 기반 추천 (관심 태그 기반).
     * 활동 데이터 없는 신규 사용자용. 관심 태그가 달린 피드를 최신순으로.
     */
    @Query(nativeQuery = true, value = """
        SELECT f.*
        FROM feeds f
        JOIN feed_tags ft ON f.id = ft.feed_id
        WHERE f.status = 'ACTIVE'
          AND ft.tag_id = ANY(:interestTagIds)
          AND f.user_id != :userId
          AND f.user_id NOT IN (
            SELECT blocked_id FROM blocks WHERE blocker_id = :userId
            UNION
            SELECT blocker_id FROM blocks WHERE blocked_id = :userId
          )
        AND (:cursorId IS NULL OR f.id < :cursorId)
        GROUP BY f.id
        ORDER BY f.id DESC
        LIMIT :size
        """)
    List<Feed> findByInterestTags(
            @Param("userId") Long userId,
            @Param("interestTagIds") Long[] interestTagIds,
            @Param("cursorId") Long cursorId,
            @Param("size") int size
    );

    /**
     * 내가 쓴 피드 조회 (커서 지원).
     */
    @Query(nativeQuery = true, value = """
        SELECT f.*
        FROM feeds f
        WHERE f.status = 'ACTIVE'
          AND f.user_id = :userId
          AND (:cursorId IS NULL OR f.id < :cursorId)
        ORDER BY f.id DESC
        LIMIT :size
        """)
    List<Feed> findMyFeeds(
            @Param("userId") Long userId,
            @Param("cursorId") Long cursorId,
            @Param("size") int size
    );

    /**
     * 내가 리포스트한 피드 조회 (커서 지원).
     * feed_reposts 테이블을 조인해서 리포스트 시간 역순으로 정렬
     */
    @Query(nativeQuery = true, value = """
        SELECT f.*
        FROM feeds f
        JOIN feed_reposts fr ON f.id = fr.feed_id
        WHERE fr.user_id = :userId
          AND f.status = 'ACTIVE'
          AND (:cursorId IS NULL OR fr.feed_id < :cursorId)
        ORDER BY fr.feed_id DESC
        LIMIT :size
        """)
    List<Feed> findMyReposts(
            @Param("userId") Long userId,
            @Param("cursorId") Long cursorId,
            @Param("size") int size
    );

        @Query(nativeQuery = true, value = """
        SELECT f.*,
            (
                (SELECT COUNT(*) FROM feed_likes fl
                WHERE fl.feed_id = f.id
                AND fl.created_at > NOW() - INTERVAL '1 hour') * 10
            + (SELECT COUNT(*) FROM feed_likes fl
                WHERE fl.feed_id = f.id
                AND fl.created_at > NOW() - INTERVAL '6 hours') * 3
            + (SELECT COUNT(*) FROM feed_likes fl
                WHERE fl.feed_id = f.id
                AND fl.created_at > NOW() - INTERVAL '24 hours') * 1
            ) AS trending_score
        FROM feeds f
        WHERE f.status = 'ACTIVE'
        AND f.created_at > NOW() - INTERVAL '7 days'
        AND (:userId IS NULL OR f.user_id NOT IN (
            SELECT blocked_id FROM blocks WHERE blocker_id = :userId
            UNION
            SELECT blocker_id FROM blocks WHERE blocked_id = :userId
        ))
        AND (:cursorId IS NULL OR f.id < :cursorId)
        ORDER BY trending_score DESC, f.id DESC
        LIMIT :size
        """)
    List<Feed> findTrendingFeeds(
            @Param("userId") Long userId,
            @Param("cursorId") Long cursorId,
            @Param("size") int size
    );

    /**
     * 최신 피드 조회 (비로그인 사용자용).
     */
    @Query(nativeQuery = true, value = """
    SELECT f.*
    FROM feeds f
    WHERE f.status = 'ACTIVE'
      AND (:userId IS NULL OR f.user_id NOT IN (
          SELECT blocked_id FROM blocks WHERE blocker_id = :userId
          UNION
          SELECT blocker_id FROM blocks WHERE blocked_id = :userId
      ))
    ORDER BY f.id DESC
    """)
    List<Feed> findLatestFeeds(@Param("userId") Long userId, Pageable pageable);

    /** 좋아요 수 +1. 엔티티 필드 갱신 대신 DB UPDATE로 동시성 처리. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Feed f SET f.likeCount = f.likeCount + 1 WHERE f.id = :feedId AND f.status = com.dipsource.entity.feed.FeedStatus.ACTIVE")
    int incrementLikeCount(@Param("feedId") Long feedId);

    /** 좋아요 수 -1 (0 미만으로 내려가지 않음). */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Feed f SET f.likeCount = f.likeCount - 1 WHERE f.id = :feedId AND f.likeCount > 0")
    int decrementLikeCount(@Param("feedId") Long feedId);

    /** 리포스트 수 +1 */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Feed f SET f.repostCount = f.repostCount + 1 WHERE f.id = :feedId AND f.status = com.dipsource.entity.feed.FeedStatus.ACTIVE")
    int incrementRepostCount(@Param("feedId") Long feedId);

    /** 리포스트 수 -1 (0 미만 방지) */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Feed f SET f.repostCount = f.repostCount - 1 WHERE f.id = :feedId AND f.repostCount > 0")
    int decrementRepostCount(@Param("feedId") Long feedId);

    /** 관리자 피드 목록. 삭제(DELETED)만 빼고, 제목(본문)·작성자 닉네임 검색. */
    @Query("""
            SELECT f FROM Feed f JOIN User u ON u.id = f.userId
            WHERE f.status <> com.dipsource.entity.feed.FeedStatus.DELETED
            AND (:q IS NULL OR LOWER(f.content) LIKE :q ESCAPE '\\'
                OR LOWER(u.nickname) LIKE :q ESCAPE '\\')
            ORDER BY f.createdAt DESC, f.id DESC
            """)
    Page<Feed> searchForAdmin(@Param("q") String q, Pageable pageable);
}
