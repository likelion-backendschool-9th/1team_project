package com.dipsource.repository.post;

import com.dipsource.entity.post.PostComment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** post_comments 테이블 접근 */
public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    /**
     * 글의 최상위 댓글 목록 (삭제된 것 포함, 최신순, 커서 페이지네이션)
     * 삭제됐고 대댓글도 없는 댓글은 Service에서 필터링
     */
    @Query(value = """
            SELECT * FROM post_comments
            WHERE post_id = :postId
              AND parent_id IS NULL
              AND (:cursor IS NULL OR id < :cursor)
            ORDER BY id DESC
            LIMIT :size
            """, nativeQuery = true)
    List<PostComment> findTopLevelCommentsIncludeDeleted(
            @Param("postId") Long postId,
            @Param("cursor") Long cursor,
            @Param("size") int size);

    /** 여러 부모 댓글의 대댓글을 한 번에 조회 (N+1 방지) */
    @Query(value = """
            SELECT * FROM post_comments
            WHERE parent_id IN (:parentIds)
              AND deleted_at IS NULL
            ORDER BY id ASC
            """, nativeQuery = true)
    List<PostComment> findRepliesByParentIds(@Param("parentIds") List<Long> parentIds);

    /** 댓글 수 +1 */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Post p SET p.commentCount = p.commentCount + 1 WHERE p.id = :postId")
    int incrementCommentCount(@Param("postId") Long postId);

    /** 댓글 수 -1 (0 미만 방지) */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Post p SET p.commentCount = p.commentCount - 1 WHERE p.id = :postId AND p.commentCount > 0")
    int decrementCommentCount(@Param("postId") Long postId);

    /** 댓글 좋아요 수 +1 */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE PostComment c SET c.likeCount = c.likeCount + 1 WHERE c.id = :commentId")
    int incrementCommentLikeCount(@Param("commentId") Long commentId);

    /** 댓글 좋아요 수 -1 (0 미만 방지) */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE PostComment c SET c.likeCount = c.likeCount - 1 WHERE c.id = :commentId AND c.likeCount > 0")
    int decrementCommentLikeCount(@Param("commentId") Long commentId);
}
