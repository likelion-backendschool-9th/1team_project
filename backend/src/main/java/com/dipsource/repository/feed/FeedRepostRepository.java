package com.dipsource.repository.feed;

import com.dipsource.entity.feed.FeedRepost;
import com.dipsource.entity.feed.FeedUserId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** feed_reposts 테이블 접근 */
public interface FeedRepostRepository extends JpaRepository<FeedRepost, FeedUserId> {

    boolean existsByIdUserIdAndIdFeedId(Long userId, Long feedId);

    /** 해당 유저가 리포스트한 피드 ID 목록 (배치 조회용) */
    @Query(value = """
            SELECT feed_id FROM feed_reposts
            WHERE user_id = :userId AND feed_id IN (:feedIds)
            """, nativeQuery = true)
    List<Long> findRepostedFeedIds(@Param("userId") Long userId, @Param("feedIds") List<Long> feedIds);

    /** 리포스트 삽입. 이미 있으면 무시 (멱등) */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            INSERT INTO feed_reposts (user_id, feed_id, created_at)
            VALUES (:userId, :feedId, NOW())
            ON CONFLICT (user_id, feed_id) DO NOTHING
            """, nativeQuery = true)
    int insertIgnore(@Param("userId") Long userId, @Param("feedId") Long feedId);

    /** 리포스트 삭제. 없으면 0 반환 (race-safe) */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            DELETE FROM feed_reposts
            WHERE user_id = :userId AND feed_id = :feedId
            """, nativeQuery = true)
    int deleteByUserIdAndFeedId(@Param("userId") Long userId, @Param("feedId") Long feedId);
}
