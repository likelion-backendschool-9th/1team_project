package com.dipsource.repository.post;

import com.dipsource.entity.post.PostBookmark;
import com.dipsource.entity.post.PostUserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** post_bookmarks 테이블 접근 */
public interface PostBookmarkRepository extends JpaRepository<PostBookmark, PostUserId> {

    /** 북마크 저장. 이미 했으면 무시한다. @return 0이면 이미 북마크, 1이면 새로 북마크 */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            INSERT INTO post_bookmarks (user_id, post_id, created_at)
            VALUES (:userId, :postId, NOW())
            ON CONFLICT (user_id, post_id) DO NOTHING
            """, nativeQuery = true)
    int insertIgnore(@Param("userId") Long userId, @Param("postId") Long postId);

    /** 북마크 삭제. 안 했으면 아무것도 지우지 않는다. @return 0이면 원래 안 함, 1이면 취소 */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM post_bookmarks WHERE user_id = :userId AND post_id = :postId", nativeQuery = true)
    int deleteByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);
}
