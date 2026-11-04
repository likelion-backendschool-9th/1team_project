package com.dipsource.repository.feed;

import com.dipsource.entity.feed.FeedBookmark;
import com.dipsource.entity.feed.FeedUserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** feed_bookmarks 테이블 접근. */
public interface FeedBookmarkRepository extends JpaRepository<FeedBookmark, FeedUserId> {

    /**
     * 북마크 삽입. 이미 (user_id, feed_id)가 있으면 무시.
     * @return 삽입된 행 수 (0이면 이미 북마크됨, 1이면 신규)
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            INSERT INTO feed_bookmarks (user_id, feed_id, created_at)
            VALUES (:userId, :feedId, NOW())
            ON CONFLICT (user_id, feed_id) DO NOTHING
            """, nativeQuery = true)
    int insertIgnore(@Param("userId") Long userId, @Param("feedId") Long feedId);
}
