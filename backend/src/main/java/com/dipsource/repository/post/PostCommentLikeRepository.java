package com.dipsource.repository.post;

import com.dipsource.entity.post.PostCommentLike;
import com.dipsource.entity.post.PostCommentLikeId;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** post_comment_likes 테이블 접근 */
public interface PostCommentLikeRepository extends JpaRepository<PostCommentLike, PostCommentLikeId> {

    /** 댓글 좋아요 저장. 이미 눌렀으면 무시한다. @return 0이면 이미 좋아요, 1이면 새로 좋아요 */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            INSERT INTO post_comment_likes (user_id, comment_id, created_at)
            VALUES (:userId, :commentId, NOW())
            ON CONFLICT (user_id, comment_id) DO NOTHING
            """, nativeQuery = true)
    int insertIgnore(@Param("userId") Long userId, @Param("commentId") Long commentId);

    /** 댓글 좋아요 삭제. @return 0이면 원래 안 누름, 1이면 취소 */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM post_comment_likes WHERE user_id = :userId AND comment_id = :commentId", nativeQuery = true)
    int deleteByUserIdAndCommentId(@Param("userId") Long userId, @Param("commentId") Long commentId);

    /** commentIds 가운데 내가 좋아요한 댓글 ID */
    @Query("SELECT l.id.commentId FROM PostCommentLike l WHERE l.id.userId = :userId AND l.id.commentId IN :commentIds")
    List<Long> findLikedCommentIds(@Param("userId") Long userId, @Param("commentIds") Collection<Long> commentIds);
}
