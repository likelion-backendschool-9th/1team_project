package com.dipsource.repository.post;

import com.dipsource.entity.post.PostComment;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** post_comments 테이블 접근 */
public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    /**
     * 글의 최상위 댓글. 오래된 순.
     * 지운 댓글이라도 살아 있는 답글이 있으면 "삭제된 댓글" 자리로 남겨서 답글이 사라지지 않게 한다.
     */
    @Query(value = """
            SELECT c FROM PostComment c
            WHERE c.post.id = :postId AND c.parent IS NULL
              AND (c.deletedAt IS NULL
                   OR EXISTS (SELECT 1 FROM PostComment r WHERE r.parent = c AND r.deletedAt IS NULL))
            ORDER BY c.id ASC
            """,
            countQuery = """
            SELECT COUNT(c) FROM PostComment c
            WHERE c.post.id = :postId AND c.parent IS NULL
              AND (c.deletedAt IS NULL
                   OR EXISTS (SELECT 1 FROM PostComment r WHERE r.parent = c AND r.deletedAt IS NULL))
            """)
    Page<PostComment> findTopLevel(@Param("postId") Long postId, Pageable pageable);

    /** 여러 부모 댓글의 살아 있는 답글. 오래된 순 */
    @Query("SELECT c FROM PostComment c WHERE c.parent.id IN :parentIds AND c.deletedAt IS NULL ORDER BY c.id ASC")
    List<PostComment> findReplies(@Param("parentIds") Collection<Long> parentIds);

    /** 댓글 좋아요 수 +1 */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE PostComment c SET c.likeCount = c.likeCount + 1 WHERE c.id = :commentId")
    int incrementLikeCount(@Param("commentId") Long commentId);

    /** 댓글 좋아요 수 -1 (0 아래로는 내리지 않는다) */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE PostComment c SET c.likeCount = c.likeCount - 1 WHERE c.id = :commentId AND c.likeCount > 0")
    int decrementLikeCount(@Param("commentId") Long commentId);
}
