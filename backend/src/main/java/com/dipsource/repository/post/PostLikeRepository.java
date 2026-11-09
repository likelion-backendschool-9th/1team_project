package com.dipsource.repository.post;

import com.dipsource.entity.post.PostLike;
import com.dipsource.entity.post.PostUserId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** post_likes 테이블 접근. */
public interface PostLikeRepository extends JpaRepository<PostLike, PostUserId> {

    /** 해당 유저가 좋아요한 글 ID 목록 (배치 조회용) */
    @Query(value = """
            SELECT post_id FROM post_likes
            WHERE user_id = :userId AND post_id IN (:postIds)
            """, nativeQuery = true)
    List<Long> findLikedPostIds(@Param("userId") Long userId, @Param("postIds") List<Long> postIds);

    /**
     * 좋아요 삽입. 이미 (user_id, post_id)가 있으면 무시.
     * @return 삽입된 행 수 (0이면 이미 좋아요됨, 1이면 신규)
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            INSERT INTO post_likes (user_id, post_id, created_at)
            VALUES (:userId, :postId, NOW())
            ON CONFLICT (user_id, post_id) DO NOTHING
            """, nativeQuery = true)
    int insertIgnore(@Param("userId") Long userId, @Param("postId") Long postId);

    /**
     * 좋아요 삭제. 행이 없으면 0 반환 (race-safe).
     * @return 삭제된 행 수 (0이면 이미 없음, 1이면 삭제됨)
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            DELETE FROM post_likes
            WHERE user_id = :userId AND post_id = :postId
            """, nativeQuery = true)
    int deleteByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);
}
