package com.dipsource.repository.feed;

import com.dipsource.entity.feed.FeedComment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** feed_comments 테이블 접근 */
public interface FeedCommentRepository extends JpaRepository<FeedComment, Long> {

    /**
     * 피드의 최상위 댓글 목록 (삭제 안 된 것, 최신순, 커서 페이지네이션)
     * parent가 null인 것만 조회
     */
    @Query(value = """
            SELECT * FROM feed_comments
            WHERE feed_id = :feedId
              AND parent_id IS NULL
              AND deleted_at IS NULL
              AND (:cursor IS NULL OR id < :cursor)
            ORDER BY id DESC
            LIMIT :size
            """, nativeQuery = true)
    List<FeedComment> findTopLevelComments(
            @Param("feedId") Long feedId,
            @Param("cursor") Long cursor,
            @Param("size") int size);

    /**
     * 삭제된 부모도 포함한 최상위 댓글 목록 (대댓글 있으면 "삭제된 댓글입니다" 표시용)
     * 삭제됐고 대댓글도 없는 건 Service에서 필터링
     */
    @Query(value = """
            SELECT * FROM feed_comments
            WHERE feed_id = :feedId
              AND parent_id IS NULL
              AND (:cursor IS NULL OR id < :cursor)
            ORDER BY id DESC
            LIMIT :size
            """, nativeQuery = true)
    List<FeedComment> findTopLevelCommentsIncludeDeleted(
            @Param("feedId") Long feedId,
            @Param("cursor") Long cursor,
            @Param("size") int size);

    /**
     * 특정 댓글의 대댓글 목록 (삭제 안 된 것, 오래된 순)
     */
    @Query(value = """
            SELECT * FROM feed_comments
            WHERE parent_id = :parentId
              AND deleted_at IS NULL
            ORDER BY id ASC
            """, nativeQuery = true)
    List<FeedComment> findReplies(@Param("parentId") Long parentId);

    /** 여러 부모 댓글의 대댓글을 한 번에 조회 (N+1 방지) */
    @Query(value = """
            SELECT * FROM feed_comments
            WHERE parent_id IN (:parentIds)
              AND deleted_at IS NULL
            ORDER BY id ASC
            """, nativeQuery = true)
    List<FeedComment> findRepliesByParentIds(@Param("parentIds") List<Long> parentIds);

    /** 댓글 수 +1 */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Feed f SET f.commentCount = f.commentCount + 1 WHERE f.id = :feedId")
    int incrementCommentCount(@Param("feedId") Long feedId);

    /** 댓글 수 -1 (0 미만 방지) */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Feed f SET f.commentCount = f.commentCount - 1 WHERE f.id = :feedId AND f.commentCount > 0")
    int decrementCommentCount(@Param("feedId") Long feedId);

    /** 댓글 좋아요 수 +1 */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE FeedComment c SET c.likeCount = c.likeCount + 1 WHERE c.id = :commentId")
    int incrementCommentLikeCount(@Param("commentId") Long commentId);

    /** 댓글 좋아요 수 -1 (0 미만 방지) */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE FeedComment c SET c.likeCount = c.likeCount - 1 WHERE c.id = :commentId AND c.likeCount > 0")
    int decrementCommentLikeCount(@Param("commentId") Long commentId);
}
