package com.dipsource.repository.feed;

import com.dipsource.entity.feed.FeedCommentLike;
import com.dipsource.entity.feed.FeedCommentLikeId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** feed_comment_likes 테이블 접근 */
public interface FeedCommentLikeRepository extends JpaRepository<FeedCommentLike, FeedCommentLikeId> {

    /** 해당 유저가 좋아요한 댓글 ID 목록 (배치 조회용) */
    @Query(value = """
            SELECT comment_id FROM feed_comment_likes
            WHERE user_id = :userId AND comment_id IN (:commentIds)
            """, nativeQuery = true)
    List<Long> findLikedCommentIds(@Param("userId") Long userId, @Param("commentIds") List<Long> commentIds);

    /** 좋아요 삽입. 이미 있으면 무시 (멱등) */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            INSERT INTO feed_comment_likes (user_id, comment_id, created_at)
            VALUES (:userId, :commentId, NOW())
            ON CONFLICT (user_id, comment_id) DO NOTHING
            """, nativeQuery = true)
    int insertIgnore(@Param("userId") Long userId, @Param("commentId") Long commentId);

    /** 좋아요 삭제. 없으면 0 반환 (race-safe) */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            DELETE FROM feed_comment_likes
            WHERE user_id = :userId AND comment_id = :commentId
            """, nativeQuery = true)
    int deleteByUserIdAndCommentId(@Param("userId") Long userId, @Param("commentId") Long commentId);
}
