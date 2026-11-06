package com.dipsource.repository.post;

import com.dipsource.entity.post.PostLike;
import com.dipsource.entity.post.PostUserId;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** post_likes 테이블 접근 */
public interface PostLikeRepository extends JpaRepository<PostLike, PostUserId> {

    /**
     * 좋아요 저장. 이미 눌렀으면 무시한다.
     * @return 저장된 행 수 (0이면 이미 좋아요, 1이면 새로 좋아요)
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            INSERT INTO post_likes (user_id, post_id, created_at)
            VALUES (:userId, :postId, NOW())
            ON CONFLICT (user_id, post_id) DO NOTHING
            """, nativeQuery = true)
    int insertIgnore(@Param("userId") Long userId, @Param("postId") Long postId);

    /**
     * 좋아요 삭제. 누르지 않았으면 아무것도 지우지 않는다.
     * @return 삭제된 행 수 (0이면 원래 안 누름, 1이면 취소)
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM post_likes WHERE user_id = :userId AND post_id = :postId", nativeQuery = true)
    int deleteByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);

    /** postIds 가운데 내가 좋아요한 글 ID (목록의 liked 표시용) */
    @Query("SELECT pl.id.postId FROM PostLike pl WHERE pl.id.userId = :userId AND pl.id.postId IN :postIds")
    List<Long> findLikedPostIds(@Param("userId") Long userId, @Param("postIds") Collection<Long> postIds);
}
