package com.dipsource.repository.feed;

import com.dipsource.entity.feed.FeedCommentLike;
import com.dipsource.entity.feed.FeedCommentLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** feed_comment_likes 테이블 접근 */
public interface FeedCommentLikeRepository extends JpaRepository<FeedCommentLike, FeedCommentLikeId> {

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
