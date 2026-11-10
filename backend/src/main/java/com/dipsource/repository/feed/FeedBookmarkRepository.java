package com.dipsource.repository.feed;

import com.dipsource.entity.feed.FeedBookmark;
import com.dipsource.entity.feed.FeedUserId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** feed_bookmarks 테이블 접근. */
public interface FeedBookmarkRepository extends JpaRepository<FeedBookmark, FeedUserId> {

    /** 해당 유저가 북마크한 피드 ID 목록 (배치 조회용) */
    @Query(value = """
            SELECT feed_id FROM feed_bookmarks
            WHERE user_id = :userId AND feed_id IN (:feedIds)
            """, nativeQuery = true)
    List<Long> findBookmarkedFeedIds(@Param("userId") Long userId, @Param("feedIds") List<Long> feedIds);

    boolean existsByIdUserIdAndIdFeedId(Long userId, Long feedId);

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

    /**
     * 북마크 삭제. 행이 없으면 0 반환 (race-safe)
     * @return 삭제된 행 수 (0이면 이미 없음, 1이면 삭제됨)
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            DELETE FROM feed_bookmarks
            WHERE user_id = :userId AND feed_id = :feedId
            """, nativeQuery = true)
    int deleteByUserIdAndFeedId(@Param("userId") Long userId, @Param("feedId") Long feedId);
}
