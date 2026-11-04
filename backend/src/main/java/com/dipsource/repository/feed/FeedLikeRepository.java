package com.dipsource.repository.feed;

import com.dipsource.entity.feed.FeedLike;
import com.dipsource.entity.feed.FeedUserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** feed_likes 테이블 접근. */
public interface FeedLikeRepository extends JpaRepository<FeedLike, FeedUserId> {

    /**
     * 좋아요 삽입. 이미 (user_id, feed_id)가 있으면 무시.
     * @return 삽입된 행 수 (0이면 이미 좋아요됨, 1이면 신규)
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            INSERT INTO feed_likes (user_id, feed_id, created_at)
            VALUES (:userId, :feedId, NOW())
            ON CONFLICT (user_id, feed_id) DO NOTHING
            """, nativeQuery = true)
    int insertIgnore(@Param("userId") Long userId, @Param("feedId") Long feedId);
}
